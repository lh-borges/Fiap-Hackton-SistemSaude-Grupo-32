package br.com.fiap.sus.consultas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.AgendarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
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
class AgendarConsultaUseCaseTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private AgendarConsultaUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID unidadeSaudeId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new AgendarConsultaUseCase(consultaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    private AgendarConsultaDTO dtoValido() {
        return new AgendarConsultaDTO(pacienteId, medicoId, unidadeSaudeId,
                Instant.now().plus(1, ChronoUnit.DAYS), "Consulta de rotina");
    }

    @Test
    @DisplayName("ATENDENTE agenda consulta para qualquer paciente")
    void atendenteAgendaParaQualquerPaciente() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(true);
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(true);
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultaOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.pacienteId()).isEqualTo(pacienteId);
        verify(consultaRepository).salvar(any(Consulta.class));
    }

    @Test
    @DisplayName("PACIENTE agenda consulta para si mesmo")
    void pacienteAgendaParaSiMesmo() {
        UUID usuarioId = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(true);
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(true);
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultaOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.pacienteId()).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("PACIENTE nao pode agendar consulta para outro paciente")
    void pacienteNaoAgendaParaOutroPaciente() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteDiferente = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteDiferente));

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(NaoAutorizadoException.class);
    }

    @Test
    @DisplayName("nao agenda se paciente estiver inativo ou nao existir")
    void naoAgendaComPacienteInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao agenda se medico estiver inativo ou nao existir")
    void naoAgendaComMedicoInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao agenda se unidade estiver inativa ou nao existir")
    void naoAgendaComUnidadeInativa() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.medicoAtivoExiste(medicoId)).thenReturn(true);
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}