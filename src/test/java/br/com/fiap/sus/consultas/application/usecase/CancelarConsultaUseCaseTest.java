package br.com.fiap.sus.consultas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.CancelarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
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
class CancelarConsultaUseCaseTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private CancelarConsultaUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID consultaId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new CancelarConsultaUseCase(consultaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    private Consulta consultaAgendada() {
        Instant agora = Instant.now();
        return Consulta.reconstituir(consultaId, pacienteId, medicoId, UUID.randomUUID(),
                Instant.now().plus(1, ChronoUnit.DAYS), SituacaoConsulta.AGENDADA, "Motivo", null, null,
                false, agora, agora);
    }

    @Test
    @DisplayName("ADMINISTRADOR cancela qualquer consulta")
    void administradorCancelaQualquerConsulta() {
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Admin", Set.of("ADMINISTRADOR"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ConsultaOutput resultado = useCase.executar(new CancelarConsultaDTO(consultaId, "Motivo"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoConsulta.CANCELADA);
    }

    @Test
    @DisplayName("MEDICO cancela a propria consulta")
    void medicoCancelaAPropria() {
        UUID usuarioId = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));

        ConsultaOutput resultado = useCase.executar(new CancelarConsultaDTO(consultaId, "Motivo"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoConsulta.CANCELADA);
    }

    @Test
    @DisplayName("MEDICO nao cancela consulta de outro medico")
    void medicoNaoCancelaDeOutroMedico() {
        UUID usuarioId = UUID.randomUUID();
        UUID medicoDiferente = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoDiferente));

        assertThatThrownBy(() -> useCase.executar(new CancelarConsultaDTO(consultaId, "Motivo")))
                .isInstanceOf(NaoAutorizadoException.class);
    }

    @Test
    @DisplayName("PACIENTE nao cancela consulta de terceiro")
    void pacienteNaoCancelaDeTerceiro() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteDiferente = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteDiferente));

        assertThatThrownBy(() -> useCase.executar(new CancelarConsultaDTO(consultaId, "Motivo")))
                .isInstanceOf(NaoAutorizadoException.class);
    }
}