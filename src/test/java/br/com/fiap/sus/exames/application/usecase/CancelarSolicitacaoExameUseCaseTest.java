package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.CancelarSolicitacaoExameDTO;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.time.Instant;
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
class CancelarSolicitacaoExameUseCaseTest {

    @Mock
    private SolicitacaoExameRepository solicitacaoExameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    @Mock
    private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    private CancelarSolicitacaoExameUseCase useCase;

    private final UUID medicoId = UUID.randomUUID();
    private final UUID solicitacaoExameId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new CancelarSolicitacaoExameUseCase(solicitacaoExameRepository, cadastroQuery,
                usuarioAutenticadoProvider);
    }

    private SolicitacaoExame solicitacaoPendenteDoMedico(UUID medicoDaSolicitacao) {
        Instant agora = Instant.now();
        return SolicitacaoExame.reconstituir(solicitacaoExameId, UUID.randomUUID(), medicoDaSolicitacao,
                UUID.randomUUID(), null, "Justificativa valida", SituacaoSolicitacaoExame.PENDENTE, null,
                agora, agora);
    }

    @Test
    @DisplayName("ADMINISTRADOR cancela qualquer solicitacao pendente")
    void administradorCancelaQualquerSolicitacao() {
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId))
                .thenReturn(Optional.of(solicitacaoPendenteDoMedico(medicoId)));
        when(solicitacaoExameRepository.salvar(any(SolicitacaoExame.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioAutenticado usuario = new UsuarioAutenticado(UUID.randomUUID(), "Admin", Set.of("ADMINISTRADOR"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);

        SolicitacaoExameOutput resultado = useCase.executar(
                new CancelarSolicitacaoExameDTO(solicitacaoExameId, "Motivo"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoSolicitacaoExame.CANCELADA);
    }

    @Test
    @DisplayName("MEDICO cancela a propria solicitacao")
    void medicoCancelaAPropria() {
        UUID usuarioId = UUID.randomUUID();
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId))
                .thenReturn(Optional.of(solicitacaoPendenteDoMedico(medicoId)));
        when(solicitacaoExameRepository.salvar(any(SolicitacaoExame.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoId));

        SolicitacaoExameOutput resultado = useCase.executar(
                new CancelarSolicitacaoExameDTO(solicitacaoExameId, "Motivo"));

        assertThat(resultado.situacao()).isEqualTo(SituacaoSolicitacaoExame.CANCELADA);
    }

    @Test
    @DisplayName("MEDICO nao cancela solicitacao de outro medico")
    void medicoNaoCancelaDeOutroMedico() {
        UUID usuarioId = UUID.randomUUID();
        UUID medicoDiferente = UUID.randomUUID();
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId))
                .thenReturn(Optional.of(solicitacaoPendenteDoMedico(medicoId)));
        UsuarioAutenticado usuario = new UsuarioAutenticado(usuarioId, "Dr. Carlos", Set.of("MEDICO"));
        when(usuarioAutenticadoProvider.obrigatorio()).thenReturn(usuario);
        when(cadastroQuery.medicoIdDoUsuario(usuarioId)).thenReturn(Optional.of(medicoDiferente));

        assertThatThrownBy(() -> useCase.executar(
                new CancelarSolicitacaoExameDTO(solicitacaoExameId, "Motivo")))
                .isInstanceOf(NaoAutorizadoException.class);
    }
}