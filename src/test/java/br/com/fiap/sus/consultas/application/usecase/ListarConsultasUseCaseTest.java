package br.com.fiap.sus.consultas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.domain.repository.ConsultaFiltro;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
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
class ListarConsultasUseCaseTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private ListarConsultasUseCase useCase;

    @BeforeEach
    void configurar() {
        useCase = new ListarConsultasUseCase(consultaRepository, cadastroQuery, usuarioAutenticadoProvider);
        when(consultaRepository.listar(any(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(PaginaResultado.de(List.of(), 0, 50, 0));
    }

    @Test
    @DisplayName("PACIENTE so ve as proprias consultas, mesmo informando outro pacienteId no filtro")
    void pacienteVeApenasAsProprias() {
        UUID usuarioId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID pacienteInformadoNoFiltro = UUID.randomUUID();
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Joao", Set.of("PACIENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.pacienteIdDoUsuario(usuarioId)).thenReturn(Optional.of(pacienteId));

        ConsultaFiltro filtroOriginal = new ConsultaFiltro(pacienteInformadoNoFiltro, null, null, null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        ArgumentCaptor<ConsultaFiltro> captor = ArgumentCaptor.forClass(ConsultaFiltro.class);
        verify(consultaRepository).listar(captor.capture(), org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(50));
        assertThat(captor.getValue().pacienteId()).isEqualTo(pacienteId);
    }

    @Test
    @DisplayName("ATENDENTE lista com o filtro original, sem sobrescrever")
    void atendenteListaComFiltroOriginal() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Ana", Set.of("ATENDENTE"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        ConsultaFiltro filtroOriginal = new ConsultaFiltro(null, null, null, null, null, null);
        useCase.executar(filtroOriginal, 0, 50);

        verify(consultaRepository).listar(filtroOriginal, 0, 50);
    }
}