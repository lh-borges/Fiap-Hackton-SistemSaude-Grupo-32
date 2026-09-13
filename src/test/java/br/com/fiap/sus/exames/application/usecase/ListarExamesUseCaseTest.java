package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.domain.repository.ExameFiltro;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
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
class ListarExamesUseCaseTest {

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private ListarExamesUseCase useCase;

    @BeforeEach
    void configurar() {
        useCase = new ListarExamesUseCase(exameRepository, cadastroQuery, usuarioAutenticadoProvider);
        when(exameRepository.listar(any(), anyInt(), anyInt()))
                .thenReturn(PaginaResultado.de(List.of(), 0, 50, 0));
    }

    @Test
    @DisplayName("PACIENTE so ve os proprios exames")
    void pacienteVeApenasOsProprios() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        ExameFiltro filtroOriginal = new ExameFiltro(UUID.randomUUID(), null, null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        ArgumentCaptor<ExameFiltro> captor = ArgumentCaptor.forClass(ExameFiltro.class);
        verify(exameRepository).listar(captor.capture(), eq(0), eq(50));
        assertThat(captor.getValue().pacienteId()).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("ATENDENTE lista com o filtro original")
    void atendenteListaComFiltroOriginal() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ExameFiltro filtroOriginal = new ExameFiltro(null, null, null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        verify(exameRepository).listar(filtroOriginal, 0, 50);
    }
}