package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.AgendarExameDTO;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendarExameUseCaseTest {

    @Mock
    private SolicitacaoExameRepository solicitacaoExameRepository;

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private CadastroQuery cadastroQuery;

    private AgendarExameUseCase useCase;

    private final UUID solicitacaoExameId = UUID.randomUUID();
    private final UUID unidadeSaudeId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new AgendarExameUseCase(solicitacaoExameRepository, exameRepository, cadastroQuery);
    }

    private SolicitacaoExame solicitacaoPendente() {
        Instant agora = Instant.now();
        return SolicitacaoExame.reconstituir(solicitacaoExameId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), null, "Justificativa valida", SituacaoSolicitacaoExame.PENDENTE, null,
                agora, agora);
    }

    private AgendarExameDTO dtoValido() {
        return new AgendarExameDTO(solicitacaoExameId, unidadeSaudeId, Instant.now().plus(2, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("agenda exame a partir de solicitacao pendente")
    void agendaComSolicitacaoPendente() {
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId)).thenReturn(Optional.of(solicitacaoPendente()));
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(true);
        when(exameRepository.existeExameAtivoParaSolicitacao(solicitacaoExameId)).thenReturn(false);

        ExameOutput resultado = useCase.executar(dtoValido());

        assertThat(resultado.situacao()).isEqualTo(SituacaoExame.AGENDADO);
        assertThat(resultado.unidadeSaudeId()).isEqualTo(unidadeSaudeId);
    }

    @Test
    @DisplayName("nao agenda se a solicitacao nao existir")
    void naoAgendaSeSolicitacaoNaoExiste() {
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao agenda se a unidade estiver inativa")
    void naoAgendaComUnidadeInativa() {
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId)).thenReturn(Optional.of(solicitacaoPendente()));
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao agenda se ja existir exame ativo para a solicitacao")
    void naoAgendaSeJaExisteExameAtivo() {
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId)).thenReturn(Optional.of(solicitacaoPendente()));
        when(cadastroQuery.unidadeAtivaExiste(unidadeSaudeId)).thenReturn(true);
        when(exameRepository.existeExameAtivoParaSolicitacao(solicitacaoExameId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(dtoValido()))
                .isInstanceOf(ConflitoException.class);
    }
}