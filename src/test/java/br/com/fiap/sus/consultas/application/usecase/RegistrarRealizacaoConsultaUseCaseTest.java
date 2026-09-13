package br.com.fiap.sus.consultas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RegistrarRealizacaoConsultaDTO;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarRealizacaoConsultaUseCaseTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private RegistrarRealizacaoConsultaUseCase useCase;

    private final UUID medicoId = UUID.randomUUID();
    private final UUID consultaId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new RegistrarRealizacaoConsultaUseCase(consultaRepository, cadastroQuery,
                usuarioAutenticadoProvider);
    }

    private Consulta consultaAgendadaComMedico(UUID medicoDaConsulta) {
        Instant agora = Instant.now();
        return Consulta.reconstituir(consultaId, UUID.randomUUID(), medicoDaConsulta, UUID.randomUUID(),
                Instant.now().minus(1, ChronoUnit.HOURS), SituacaoConsulta.AGENDADA, "Motivo", null, null,
                false, agora, agora);
    }

    @Test
    @DisplayName("medico da consulta registra a realizacao")
    void medicoDaConsultaRegistraRealizacao() {
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendadaComMedico(medicoId)));
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));

        ConsultaOutput resultado = useCase.executar(
                new RegistrarRealizacaoConsultaDTO(consultaId, "Paciente atendido"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoConsulta.REALIZADA);
    }

    @Test
    @DisplayName("outro medico nao registra realizacao de consulta alheia")
    void outroMedicoNaoRegistraRealizacao() {
        UUID medicoDaConsulta = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId))
                .thenReturn(Optional.of(consultaAgendadaComMedico(medicoDaConsulta)));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));

        assertThatThrownBy(() -> useCase.executar(
                new RegistrarRealizacaoConsultaDTO(consultaId, "Paciente atendido")))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}