package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.application.dto.SolicitarExameDTO;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
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
class SolicitarExameUseCaseTest {

    @Mock
    private SolicitacaoExameRepository solicitacaoExameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private SolicitarExameUseCase useCase;

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID tipoExameId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new SolicitarExameUseCase(solicitacaoExameRepository, cadastroQuery,
                usuarioAutenticadoProvider);
    }

    private SolicitarExameDTO dtoValido() {
        return new SolicitarExameDTO(pacienteId, tipoExameId, null, "Suspeita de anemia");
    }

    @Test
    @DisplayName("medico solicita exame para paciente e tipo de exame ativos")
    void medicoSolicitaComDadosValidos() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.tipoExameAtivoExiste(tipoExameId)).thenReturn(true);
        when(solicitacaoExameRepository.salvar(any(SolicitacaoExame.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitacaoExameOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.medicoId()).isEqualTo(medicoId);
        assertThat(resultado.situacao()).isEqualTo(SituacaoSolicitacaoExame.PENDENTE);
    }

    @Test
    @DisplayName("nao solicita se paciente estiver inativo ou nao existir")
    void naoSolicitaComPacienteInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao solicita se tipo de exame estiver inativo ou nao existir")
    void naoSolicitaComTipoExameInativo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));
        when(cadastroQuery.pacienteAtivoExiste(pacienteId)).thenReturn(true);
        when(cadastroQuery.tipoExameAtivoExiste(tipoExameId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}