package br.com.fiap.sus.exames.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExameTest {

    private final UUID solicitacaoExameId = UUID.randomUUID();
    private final UUID unidadeSaudeId = UUID.randomUUID();

    // ---------- agendar ----------

    @Test
    @DisplayName("agenda exame com data futura")
    void deveAgendarExameComDataFutura() {
        Instant dataFutura = Instant.now().plus(2, ChronoUnit.DAYS);

        Exame exame = Exame.agendar(solicitacaoExameId, unidadeSaudeId, dataFutura);

        assertThat(exame.getSolicitacaoExameId()).isEqualTo(solicitacaoExameId);
        assertThat(exame.getSituacao()).isEqualTo(SituacaoExame.AGENDADO);
        assertThat(exame.getDataAgendada()).isEqualTo(dataFutura);
    }

    @Test
    @DisplayName("nao agenda exame com data no passado")
    void naoDeveAgendarComDataNoPassado() {
        Instant dataPassada = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> Exame.agendar(solicitacaoExameId, unidadeSaudeId, dataPassada))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- registrarRealizacao ----------

    @Test
    @DisplayName("registra realizacao de exame agendado")
    void deveRegistrarRealizacaoDeExameAgendado() {
        Exame exame = exameComSituacao(SituacaoExame.AGENDADO, Instant.now().plus(1, ChronoUnit.DAYS));
        Instant dataRealizacao = Instant.now().minus(1, ChronoUnit.HOURS);

        exame.registrarRealizacao(dataRealizacao);

        assertThat(exame.getSituacao()).isEqualTo(SituacaoExame.REALIZADO);
        assertThat(exame.getDataRealizacao()).isEqualTo(dataRealizacao);
    }

    @Test
    @DisplayName("nao registra realizacao de exame ja realizado")
    void naoDeveRegistrarRealizacaoDeExameJaRealizado() {
        Exame exame = exameComSituacao(SituacaoExame.REALIZADO, Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> exame.registrarRealizacao(Instant.now()))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao registra realizacao com data futura")
    void naoDeveRegistrarRealizacaoComDataFutura() {
        Exame exame = exameComSituacao(SituacaoExame.AGENDADO, Instant.now().plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> exame.registrarRealizacao(Instant.now().plus(1, ChronoUnit.HOURS)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- cancelar ----------

    @Test
    @DisplayName("cancela exame agendado")
    void deveCancelarExameAgendado() {
        Exame exame = exameComSituacao(SituacaoExame.AGENDADO, Instant.now().plus(1, ChronoUnit.DAYS));

        exame.cancelar();

        assertThat(exame.getSituacao()).isEqualTo(SituacaoExame.CANCELADO);
    }

    @Test
    @DisplayName("nao cancela exame ja realizado")
    void naoDeveCancelarExameRealizado() {
        Exame exame = exameComSituacao(SituacaoExame.REALIZADO, Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(exame::cancelar)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela exame ja cancelado")
    void naoDeveCancelarExameJaCancelado() {
        Exame exame = exameComSituacao(SituacaoExame.CANCELADO, Instant.now().plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(exame::cancelar)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- helper ----------

    private Exame exameComSituacao(SituacaoExame situacao, Instant dataAgendada) {
        Instant agora = Instant.now();
        return Exame.reconstituir(UUID.randomUUID(), solicitacaoExameId, unidadeSaudeId, dataAgendada,
                null, situacao, agora, agora);
    }
}