package br.com.fiap.sus.notificacoes;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.application.event.ConsultaAgendadaEvent;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.resultados.application.event.ResultadoExameDisponivelEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Teste de modulo (Artigo VII.1): sobe apenas notificacoes + shared, com H2 e o registro
 * de publicacao do Modulith, e publica os eventos como os produtores fariam. Nao ha Kafka:
 * e exatamente o cenario da HU-05 (canal de mensageria indisponivel).
 */
@ApplicationModuleTest(extraIncludes = "shared")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:notificacoes;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.modulith.events.jdbc.schema-initialization.enabled=true",
        "spring.modulith.events.externalization.enabled=false",
        "sus.seed.enabled=false"})
@DisplayName("Modulo notificacoes: do evento a notificacao, sem Kafka")
class NotificacoesModuloTest {
    @MockitoBean CadastroQuery cadastros;
    @Autowired NotificacaoRepository notificacoes;
    @Autowired CompletedEventPublications concluidas;
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID usuarioPaciente = UUID.randomUUID();
    final UUID usuarioMedico = UUID.randomUUID();

    @BeforeEach
    void vinculos() {
        when(cadastros.resumoDoPaciente(paciente)).thenReturn(Optional.of(
                new PacienteResumo(paciente, usuarioPaciente, "Ana", "52998224725", "ana@example.org", true)));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, usuarioMedico, "Dr. Joao", "joao@example.org", "12345", "SP",
                        UUID.randomUUID(), "Clinica Geral", true)));
    }

    @Test
    @DisplayName("HU-01/RN-02/RN-08: consulta agendada notifica paciente e medico, nao lida e com referencia")
    void consultaAgendadaNotificaPacienteEMedico(Scenario scenario) {
        var consulta = UUID.randomUUID();
        var evento = new ConsultaAgendadaEvent(UUID.randomUUID(), Instant.now(), consulta, paciente, medico,
                UUID.randomUUID(), Instant.parse("2026-09-30T17:00:00Z"));
        long pacienteAntes = notificacoes.contarNaoLidas(usuarioPaciente);
        long medicoAntes = notificacoes.contarNaoLidas(usuarioMedico);

        scenario.publish(evento)
                .andWaitForStateChange(() -> notificacoes.contarNaoLidas(usuarioMedico), n -> n == medicoAntes + 1)
                .andVerify(n -> {
                    assertThat(notificacoes.contarNaoLidas(usuarioPaciente)).isEqualTo(pacienteAntes + 1);
                    var doPaciente = notificacoes.listar(NotificacaoFiltro.todas(usuarioPaciente), 0, 50).conteudo()
                            .stream().filter(x -> evento.eventoId().equals(x.getEventoId())).toList();
                    assertThat(doPaciente).hasSize(1);
                    Notificacao np = doPaciente.get(0);
                    assertThat(np.getTipo()).isEqualTo(TipoNotificacao.CONSULTA_AGENDADA);
                    assertThat(np.getReferenciaId()).isEqualTo(consulta);
                    assertThat(np.isLida()).isFalse();
                    assertThat(np.getMensagem()).isEqualTo("Sua consulta foi agendada para 30/09/2026 14:00.");

                    var doMedico = notificacoes.listar(NotificacaoFiltro.todas(usuarioMedico), 0, 50).conteudo()
                            .stream().filter(x -> evento.eventoId().equals(x.getEventoId())).toList();
                    assertThat(doMedico).hasSize(1);
                    assertThat(doMedico.get(0).getTitulo()).isEqualTo("Nova consulta na sua agenda");
                });
    }

    @Test
    @DisplayName("HU-04/RN-03: o mesmo evento entregue duas vezes nao duplica notificacao")
    void reentregaNaoDuplica(Scenario scenario) {
        var evento = new ConsultaAgendadaEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID(), paciente, medico,
                UUID.randomUUID(), Instant.parse("2026-10-01T13:00:00Z"));
        long antes = notificacoes.contarNaoLidas(usuarioPaciente);
        long publicacoesAntes = concluidas.findAll().size();

        scenario.publish(evento)
                .andWaitForStateChange(() -> notificacoes.contarNaoLidas(usuarioPaciente), n -> n == antes + 1);

        scenario.publish(evento)
                .andWaitAtMost(Duration.ofSeconds(10))
                .andWaitForStateChange(() -> (long) concluidas.findAll().size(), n -> n >= publicacoesAntes + 2)
                .andVerify(n -> assertThat(notificacoes.contarNaoLidas(usuarioPaciente)).isEqualTo(antes + 1));
    }

    @Test
    @DisplayName("RN-09/RN-10: resultado disponivel notifica paciente e medico solicitante; sem medico, so o paciente")
    void resultadoDisponivel(Scenario scenario) {
        var outroPaciente = UUID.randomUUID();
        var usuarioOutro = UUID.randomUUID();
        when(cadastros.resumoDoPaciente(outroPaciente)).thenReturn(Optional.of(
                new PacienteResumo(outroPaciente, usuarioOutro, "Bia", "11144477735", "bia@example.org", true)));
        long medicoAntes = notificacoes.contarNaoLidas(usuarioMedico);

        var comMedico = new ResultadoExameDisponivelEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID(),
                UUID.randomUUID(), paciente, medico, "IMAGEM");
        scenario.publish(comMedico)
                .andWaitForStateChange(() -> notificacoes.contarNaoLidas(usuarioMedico), n -> n == medicoAntes + 1)
                .andVerify(n -> assertThat(notificacoes.listar(new NotificacaoFiltro(usuarioMedico, false), 0, 10)
                        .conteudo()).anyMatch(x -> x.getTipo() == TipoNotificacao.RESULTADO_DISPONIVEL));

        var semMedico = new ResultadoExameDisponivelEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID(),
                UUID.randomUUID(), outroPaciente, null, "LABORATORIAL");
        scenario.publish(semMedico)
                .andWaitForStateChange(() -> notificacoes.contarNaoLidas(usuarioOutro), n -> n == 1L)
                .andVerify(n -> assertThat(notificacoes.contarNaoLidas(usuarioMedico)).isEqualTo(medicoAntes + 1));
    }
}
