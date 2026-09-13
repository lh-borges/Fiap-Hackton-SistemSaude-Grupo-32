package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameFiltro;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
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
class ListarSolicitacoesExameUseCaseTest {

    @Mock
    private SolicitacaoExameRepository solicitacaoExameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private ListarSolicitacoesExameUseCase useCase;

    @BeforeEach
    void configurar() {
        useCase = new ListarSolicitacoesExameUseCase(solicitacaoExameRepository, cadastroQuery,
                usuarioAutenticadoProvider);
        when(solicitacaoExameRepository.listar(any(), anyInt(), anyInt()))
                .thenReturn(PaginaResultado.de(List.of(), 0, 50, 0));
    }

    @Test
    @DisplayName("PACIENTE so ve as proprias solicitacoes")
    void pacienteVeApenasAsProprias() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        SolicitacaoExameFiltro filtroOriginal = new SolicitacaoExameFiltro(UUID.randomUUID(), null, null,
                null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        ArgumentCaptor<SolicitacaoExameFiltro> captor = ArgumentCaptor.forClass(SolicitacaoExameFiltro.class);
        verify(solicitacaoExameRepository).listar(captor.capture(), eq(0), eq(50));
        assertThat(captor.getValue().pacienteId()).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("MEDICO so ve as proprias solicitacoes")
    void medicoVeApenasAsProprias() {
        UUID usuarioId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));

        SolicitacaoExameFiltro filtroOriginal = new SolicitacaoExameFiltro(null, UUID.randomUUID(), null,
                null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        ArgumentCaptor<SolicitacaoExameFiltro> captor = ArgumentCaptor.forClass(SolicitacaoExameFiltro.class);
        verify(solicitacaoExameRepository).listar(captor.capture(), eq(0), eq(50));
        assertThat(captor.getValue().medicoId()).isEqualTo(medicoId);
    }

    @Test
    @DisplayName("ADMINISTRADOR lista com o filtro original")
    void administradorListaComFiltroOriginal() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Admin", Set.of("ADMINISTRADOR"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        SolicitacaoExameFiltro filtroOriginal = new SolicitacaoExameFiltro(null, null, null, null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        verify(solicitacaoExameRepository).listar(filtroOriginal, 0, 50);
    }
}