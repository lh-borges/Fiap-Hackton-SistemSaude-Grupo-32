package br.com.fiap.sus.consultas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RemarcarConsultaDTO;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
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
class RemarcarConsultaUseCaseTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private RemarcarConsultaUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID consultaId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new RemarcarConsultaUseCase(consultaRepository, cadastroQuery, usuarioAutenticadoProvider);
    }

    private Consulta consultaAgendada() {
        Instant agora = Instant.now();
        return Consulta.reconstituir(consultaId, pacienteId, UUID.randomUUID(), UUID.randomUUID(),
                Instant.now().plus(1, ChronoUnit.DAYS), SituacaoConsulta.AGENDADA, "Motivo", null, null,
                false, agora, agora);
    }

    @Test
    @DisplayName("ATENDENTE remarca qualquer consulta")
    void atendenteRemarcaQualquerConsulta() {
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultaOutput resultado = useCase.executar(
                new RemarcarConsultaDTO(consultaId, Instant.now().plus(5, ChronoUnit.DAYS)));

        assertThat(resultado.remarcada()).isTrue();
    }

    @Test
    @DisplayName("PACIENTE remarca a propria consulta")
    void pacienteRemarcaAPropria() {
        UUID usuarioId = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));
        when(consultaRepository.salvar(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultaOutput resultado = useCase.executar(
                new RemarcarConsultaDTO(consultaId, Instant.now().plus(5, ChronoUnit.DAYS)));

        assertThat(resultado.remarcada()).isTrue();
    }

    @Test
    @DisplayName("PACIENTE nao remarca consulta de terceiro")
    void pacienteNaoRemarcaDeTerceiro() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteDiferente = UUID.randomUUID();
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.of(consultaAgendada()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteDiferente));

        assertThatThrownBy(() -> useCase.executar(
                new RemarcarConsultaDTO(consultaId, Instant.now().plus(5, ChronoUnit.DAYS))))
                .isInstanceOf(NaoAutorizadoException.class);
    }

    @Test
    @DisplayName("lanca excecao se a consulta nao existir")
    void lancaExcecaoSeConsultaNaoExiste() {
        when(consultaRepository.buscarPorId(consultaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(
                new RemarcarConsultaDTO(consultaId, Instant.now().plus(5, ChronoUnit.DAYS))))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}