package br.com.fiap.sus.resultados.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.List;
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
class ConsultarResultadoUseCaseTest {

    @Mock
    private ResultadoExameRepository resultadoExameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private ConsultarResultadoUseCase useCase;

    private final UUID resultadoId = UUID.randomUUID();
    private final UUID pacienteId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new ConsultarResultadoUseCase(resultadoExameRepository, cadastroQuery,
                usuarioAutenticadoProvider);
    }

    private ResultadoExame resultadoDoPaciente() {
        return ResultadoExame.reconstituir(resultadoId, UUID.randomUUID(), pacienteId,
                br.com.fiap.sus.resultados.domain.enums.TipoResultado.IMAGEM, java.time.Instant.now(),
                null, "url", "descricao", "laudo", List.of());
    }

    @Test
    @DisplayName("MEDICO consulta qualquer resultado")
    void medicoConsultaQualquerResultado() {
        when(resultadoExameRepository.buscarPorId(resultadoId)).thenReturn(Optional.of(resultadoDoPaciente()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ResultadoExameOutput resultado = useCase.executar(resultadoId);

        assertThat(resultado.id()).isEqualTo(resultadoId);
    }

    @Test
    @DisplayName("PACIENTE consulta o proprio resultado")
    void pacienteConsultaOProprio() {
        UUID usuarioId = UUID.randomUUID();
        when(resultadoExameRepository.buscarPorId(resultadoId)).thenReturn(Optional.of(resultadoDoPaciente()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        ResultadoExameOutput resultado = useCase.executar(resultadoId);

        assertThat(resultado.id()).isEqualTo(resultadoId);
    }

    @Test
    @DisplayName("PACIENTE de terceiro recebe RecursoNaoEncontrado, nao NaoAutorizado (EX-06)")
    void pacienteDeTerceiroRecebeNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteDiferente = UUID.randomUUID();
        when(resultadoExameRepository.buscarPorId(resultadoId)).thenReturn(Optional.of(resultadoDoPaciente()));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteDiferente));

        assertThatThrownBy(() -> useCase.executar(resultadoId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("lanca excecao se o resultado nao existir")
    void lancaExcecaoSeResultadoNaoExiste() {
        when(resultadoExameRepository.buscarPorId(resultadoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(resultadoId))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}