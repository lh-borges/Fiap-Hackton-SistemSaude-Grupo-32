package br.com.fiap.sus.receitas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReceitaTest {

    private final UUID pacienteId = UUID.randomUUID();
    private final UUID medicoId = UUID.randomUUID();
    private final UUID consultaId = UUID.randomUUID();
    private final List<ItemReceita> itens = List.of(
            ItemReceita.criar("Paracetamol", "500mg", "8 em 8 horas", "5 dias", null));

    // ---------- emitir ----------

    @Test
    @DisplayName("emite receita com dados validos, nasce ATIVA")
    void deveEmitirReceitaComDadosValidos() {
        Instant validade = Instant.now().plus(30, ChronoUnit.DAYS);

        Receita receita = Receita.emitir(pacienteId, medicoId, consultaId, itens, validade, "Observacao");

        assertThat(receita.getPacienteId()).isEqualTo(pacienteId);
        assertThat(receita.getSituacao()).isEqualTo(SituacaoReceita.ATIVA);
        assertThat(receita.getItens()).hasSize(1);
        assertThat(receita.estaVencida()).isFalse();
    }

    @Test
    @DisplayName("nao emite receita sem nenhum item")
    void naoDeveEmitirSemItens() {
        Instant validade = Instant.now().plus(30, ChronoUnit.DAYS);

        assertThatThrownBy(() -> Receita.emitir(pacienteId, medicoId, consultaId, List.of(), validade,
                "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao emite receita com validade anterior ou igual a emissao")
    void naoDeveEmitirComValidadeNoPassado() {
        Instant validadeNoPassado = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> Receita.emitir(pacienteId, medicoId, consultaId, itens, validadeNoPassado,
                "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- estaVencida ----------

    @Test
    @DisplayName("receita ativa com validade no passado esta vencida")
    void deveConsiderarVencidaQuandoValidadePassou() {
        Receita receita = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

        assertThat(receita.estaVencida()).isTrue();
    }

    @Test
    @DisplayName("receita cancelada nunca e considerada vencida")
    void receitaCanceladaNaoEhVencida() {
        Receita receita = receitaComSituacao(SituacaoReceita.CANCELADA,
                Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

        assertThat(receita.estaVencida()).isFalse();
    }

    // ---------- renovar ----------

    @Test
    @DisplayName("renova receita ativa e nao vencida")
    void deveRenovarReceitaAtiva() {
        Receita original = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));
        Instant novaValidade = Instant.now().plus(30, ChronoUnit.DAYS);

        Receita renovada = original.renovar(consultaId, novaValidade, "Renovacao de rotina");

        assertThat(original.getSituacao()).isEqualTo(SituacaoReceita.RENOVADA);
        assertThat(renovada.getSituacao()).isEqualTo(SituacaoReceita.ATIVA);
        assertThat(renovada.getReceitaOrigemId()).isEqualTo(original.getId());
        assertThat(renovada.getItens()).isEqualTo(original.getItens());
    }

    @Test
    @DisplayName("nao renova receita vencida")
    void naoDeveRenovarReceitaVencida() {
        Receita receita = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));
        Instant novaValidade = Instant.now().plus(30, ChronoUnit.DAYS);

        assertThatThrownBy(() -> receita.renovar(consultaId, novaValidade, "Renovacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao renova receita ja cancelada")
    void naoDeveRenovarReceitaCancelada() {
        Receita receita = receitaComSituacao(SituacaoReceita.CANCELADA,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));
        Instant novaValidade = Instant.now().plus(30, ChronoUnit.DAYS);

        assertThatThrownBy(() -> receita.renovar(consultaId, novaValidade, "Renovacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- cancelar ----------

    @Test
    @DisplayName("medico autor cancela a propria receita")
    void deveCancelarComMedicoAutor() {
        Receita receita = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));

        receita.cancelar(medicoId, "Paciente relatou reacao adversa");

        assertThat(receita.getSituacao()).isEqualTo(SituacaoReceita.CANCELADA);
        assertThat(receita.getMotivoCancelamento()).isEqualTo("Paciente relatou reacao adversa");
    }

    @Test
    @DisplayName("nao cancela se o medico nao for o autor")
    void naoDeveCancelarComMedicoDiferente() {
        Receita receita = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));
        UUID outroMedicoId = UUID.randomUUID();

        assertThatThrownBy(() -> receita.cancelar(outroMedicoId, "Motivo qualquer"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela receita ja cancelada")
    void naoDeveCancelarReceitaJaCancelada() {
        Receita receita = receitaComSituacao(SituacaoReceita.CANCELADA,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));

        assertThatThrownBy(() -> receita.cancelar(medicoId, "Motivo qualquer"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cancela sem motivo")
    void naoDeveCancelarSemMotivo() {
        Receita receita = receitaComSituacao(SituacaoReceita.ATIVA,
                Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(20, ChronoUnit.DAYS));

        assertThatThrownBy(() -> receita.cancelar(medicoId, ""))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- helper ----------

    private Receita receitaComSituacao(SituacaoReceita situacao, Instant dataEmissao, Instant validade) {
        return Receita.reconstituir(UUID.randomUUID(), pacienteId, medicoId, consultaId, itens, dataEmissao,
                validade, "Observacao", situacao, null, null);
    }
}