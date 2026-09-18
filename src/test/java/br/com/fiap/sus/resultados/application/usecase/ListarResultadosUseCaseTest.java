package br.com.fiap.sus.resultados.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.resultados.api.IndicadorParecerQuery;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameFiltro;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarResultadosUseCaseTest {

    @Mock
    private ResultadoExameRepository resultadoExameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    @Mock
    private IndicadorParecerQuery indicadorParecerQuery;

    private ListarResultadosUseCase useCase;

    @BeforeEach
    void configurar() {
        useCase = new ListarResultadosUseCase(resultadoExameRepository, cadastroQuery, usuarioAutenticadoProvider,
                indicadorParecerQuery);
        when(resultadoExameRepository.listar(any(), anyInt(), anyInt()))
                .thenReturn(PaginaResultado.de(List.of(), 0, 50, 0));
    }

    @Test
    @DisplayName("PACIENTE so ve os proprios resultados")
    void pacienteVeApenasOsProprios() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        ResultadoExameFiltro filtroOriginal = new ResultadoExameFiltro(UUID.randomUUID(), null, null);
        useCase.executar(filtroOriginal, 0, 50);

        ArgumentCaptor<ResultadoExameFiltro> captor = ArgumentCaptor.forClass(ResultadoExameFiltro.class);
        verify(resultadoExameRepository).listar(captor.capture(), eq(0), eq(50));
        assertThat(captor.getValue().pacienteId()).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("MEDICO lista com o filtro original")
    void medicoListaComFiltroOriginal() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ResultadoExameFiltro filtroOriginal = new ResultadoExameFiltro(null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        verify(resultadoExameRepository).listar(filtroOriginal, 0, 50);
    }
}
