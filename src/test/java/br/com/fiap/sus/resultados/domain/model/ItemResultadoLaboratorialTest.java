package br.com.fiap.sus.resultados.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemResultadoLaboratorialTest {

    // ---------- quantitativo: calculo automatico de situacao ----------

    @Test
    @DisplayName("valor dentro da faixa gera situacao NORMAL")
    void deveCalcularNormalQuandoDentroDaFaixa() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Hemoglobina", 14.2, "g/dL",
                13.0, 17.0);

        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.NORMAL);
    }

    @Test
    @DisplayName("valor abaixo do minimo gera situacao ABAIXO_REFERENCIA")
    void deveCalcularAbaixoDaReferencia() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Hemoglobina", 10.0, "g/dL",
                13.0, 17.0);

        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.ABAIXO_REFERENCIA);
    }

    @Test
    @DisplayName("valor acima do maximo gera situacao ACIMA_REFERENCIA")
    void deveCalcularAcimaDaReferencia() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Glicemia", 135.0, "mg/dL",
                70.0, 99.0);

        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.ACIMA_REFERENCIA);
    }

    @Test
    @DisplayName("valor exatamente no limite minimo e considerado NORMAL")
    void deveConsiderarNormalNoLimiteMinimo() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Hemoglobina", 13.0, "g/dL",
                13.0, 17.0);

        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.NORMAL);
    }

    @Test
    @DisplayName("valor exatamente no limite maximo e considerado NORMAL")
    void deveConsiderarNormalNoLimiteMaximo() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Hemoglobina", 17.0, "g/dL",
                13.0, 17.0);

        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.NORMAL);
    }

    @Test
    @DisplayName("quantitativo sem faixa de referencia nao calcula situacao")
    void naoDeveCalcularSituacaoSemFaixaDeReferencia() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.quantitativo("Parametro sem faixa", 5.0,
                "unidade", null, null);

        assertThat(item.getSituacao()).isNull();
    }

    // ---------- qualitativo ----------

    @Test
    @DisplayName("qualitativo aceita situacao informada explicitamente")
    void deveAceitarQualitativoComSituacaoInformada() {
        ItemResultadoLaboratorial item = ItemResultadoLaboratorial.qualitativo("HIV", "Nao reagente",
                SituacaoParametro.NORMAL);

        assertThat(item.getResultadoTexto()).isEqualTo("Nao reagente");
        assertThat(item.getSituacao()).isEqualTo(SituacaoParametro.NORMAL);
    }

    @Test
    @DisplayName("nao cria qualitativo sem situacao informada")
    void naoDeveCriarQualitativoSemSituacao() {
        assertThatThrownBy(() -> ItemResultadoLaboratorial.qualitativo("HIV", "Nao reagente", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- validacoes gerais ----------

    @Test
    @DisplayName("nao cria item sem valor numerico e sem texto")
    void naoDeveCriarSemValorESemTexto() {
        assertThatThrownBy(() -> ItemResultadoLaboratorial.qualitativo("Parametro vazio", null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria item com faixa de referencia invertida")
    void naoDeveCriarComFaixaInvertida() {
        assertThatThrownBy(() -> ItemResultadoLaboratorial.quantitativo("Parametro invalido", 10.0, "mg/dL",
                20.0, 10.0))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria item com nome de parametro muito curto")
    void naoDeveCriarComNomeCurto() {
        assertThatThrownBy(() -> ItemResultadoLaboratorial.qualitativo("A", "texto", SituacaoParametro.NORMAL))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}