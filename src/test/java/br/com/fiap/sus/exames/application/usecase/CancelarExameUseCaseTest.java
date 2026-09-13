package br.com.fiap.sus.exames.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.fiap.sus.exames.application.dto.CancelarExameDTO;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
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
class CancelarExameUseCaseTest {

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private SolicitacaoExameRepository solicitacaoExameRepository;

    private CancelarExameUseCase useCase;

    private final UUID exameId = UUID.randomUUID();
    private final UUID solicitacaoExameId = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new CancelarExameUseCase(exameRepository, solicitacaoExameRepository);
    }

    private Exame exameAgendado() {
        Instant agora = Instant.now();
        return Exame.reconstituir(exameId, solicitacaoExameId, UUID.randomUUID(),
                Instant.now().plus(1, ChronoUnit.DAYS), null, SituacaoExame.AGENDADO, agora, agora);
    }

    private SolicitacaoExame solicitacaoAgendada() {
        Instant agora = Instant.now();
        return SolicitacaoExame.reconstituir(solicitacaoExameId, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), null, "Justificativa valida", SituacaoSolicitacaoExame.AGENDADA, null,
                agora, agora);
    }

    @Test
    @DisplayName("cancela exame agendado e devolve solicitacao para pendente")
    void cancelaExameEDevolveSolicitacaoParaPendente() {
        when(exameRepository.buscarPorId(exameId)).thenReturn(Optional.of(exameAgendado()));
        when(solicitacaoExameRepository.buscarPorId(solicitacaoExameId)).thenReturn(Optional.of(solicitacaoAgendada()));

        ExameOutput resultado = useCase.executar(new CancelarExameDTO(exameId));

        assertThat(resultado.situacao()).isEqualTo(SituacaoExame.CANCELADO);
    }

    @Test
    @DisplayName("nao cancela se o exame nao existir")
    void naoCancelaSeExameNaoExiste() {
        when(exameRepository.buscarPorId(exameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new CancelarExameDTO(exameId)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}