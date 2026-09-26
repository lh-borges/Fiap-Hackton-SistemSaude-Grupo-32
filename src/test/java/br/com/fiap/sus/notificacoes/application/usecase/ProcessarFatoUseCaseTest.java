package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.Destinatario;
import br.com.fiap.sus.notificacoes.application.dto.FatoNotificavel;
import br.com.fiap.sus.notificacoes.application.dto.PapelDestinatario;
import br.com.fiap.sus.notificacoes.application.mensagem.CatalogoMensagens;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.EventoProcessadoRepository;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarFatoUseCaseTest {
    @Mock NotificacaoRepository notificacoes;
    @Mock EventoProcessadoRepository eventos;
    ProcessarFatoUseCase useCase;
    final UUID evento = UUID.randomUUID();
    final UUID consulta = UUID.randomUUID();
    final UUID usuarioPaciente = UUID.randomUUID();
    final UUID usuarioMedico = UUID.randomUUID();
    final Instant dataHora = Instant.parse("2026-09-30T17:00:00Z");

    @BeforeEach
    void configurar() {
        useCase = new ProcessarFatoUseCase(notificacoes, eventos, new CatalogoMensagens());
    }

    FatoNotificavel consultaAgendada(Destinatario... destinatarios) {
        return new FatoNotificavel(evento, "ConsultaAgendadaEvent", TipoNotificacao.CONSULTA_AGENDADA, consulta,
                List.of(destinatarios), Map.of(CatalogoMensagens.DATA_HORA, dataHora));
    }

    @Test
    @DisplayName("RN-02: um fato gera uma notificacao por destinatario, com texto por papel")
    void geraUmaNotificacaoPorDestinatario() {
        when(eventos.jaProcessado(evento)).thenReturn(false);
        when(notificacoes.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        int criadas = useCase.executar(consultaAgendada(
                new Destinatario(PapelDestinatario.PACIENTE, usuarioPaciente),
                new Destinatario(PapelDestinatario.MEDICO, usuarioMedico)));

        assertThat(criadas).isEqualTo(2);
        var captor = ArgumentCaptor.forClass(Notificacao.class);
        var ordem = inOrder(eventos, notificacoes);
        ordem.verify(eventos).registrar(eq(evento), eq("ConsultaAgendadaEvent"), any(Instant.class));
        ordem.verify(notificacoes, times(2)).salvar(captor.capture());

        var doPaciente = captor.getAllValues().get(0);
        var doMedico = captor.getAllValues().get(1);
        assertThat(doPaciente.getUsuarioId()).isEqualTo(usuarioPaciente);
        assertThat(doPaciente.getEventoId()).isEqualTo(evento);
        assertThat(doPaciente.getTipo()).isEqualTo(TipoNotificacao.CONSULTA_AGENDADA);
        assertThat(doPaciente.getReferenciaId()).isEqualTo(consulta);
        assertThat(doPaciente.isLida()).isFalse();
        assertThat(doPaciente.getMensagem()).contains("30/09/2026 14:00").startsWith("Sua consulta");
        assertThat(doMedico.getUsuarioId()).isEqualTo(usuarioMedico);
        assertThat(doMedico.getTitulo()).isEqualTo("Nova consulta na sua agenda");
    }

    @Test
    @DisplayName("HU-04/RN-03/EX-01: fato ja processado nao gera nada")
    void descartaFatoDuplicado() {
        when(eventos.jaProcessado(evento)).thenReturn(true);

        int criadas = useCase.executar(consultaAgendada(new Destinatario(PapelDestinatario.PACIENTE, usuarioPaciente)));

        assertThat(criadas).isZero();
        verify(eventos, never()).registrar(any(), any(), any());
        verifyNoInteractions(notificacoes);
    }

    @Test
    @DisplayName("RN-10/EX-02: destinatario sem usuario e descartado sem afetar os demais")
    void descartaDestinatarioSemUsuario() {
        when(eventos.jaProcessado(evento)).thenReturn(false);
        when(notificacoes.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        int criadas = useCase.executar(consultaAgendada(
                new Destinatario(PapelDestinatario.PACIENTE, null),
                new Destinatario(PapelDestinatario.MEDICO, usuarioMedico)));

        assertThat(criadas).isEqualTo(1);
        var captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes).salvar(captor.capture());
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(usuarioMedico);
        verify(eventos).registrar(eq(evento), any(), any());
    }

    @Test
    @DisplayName("fato sem destinatario resolvido ainda e marcado como processado")
    void marcaProcessadoMesmoSemDestinatarios() {
        when(eventos.jaProcessado(evento)).thenReturn(false);

        int criadas = useCase.executar(consultaAgendada());

        assertThat(criadas).isZero();
        verify(eventos).registrar(eq(evento), any(), any());
        verifyNoInteractions(notificacoes);
    }
}
