package br.com.fiap.sus.resultados.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultadoExameTest {

    private final UUID exameId = UUID.randomUUID();
    private final UUID pacienteId = UUID.randomUUID();

    // ---------- criarImagem ----------

    @Test
    @DisplayName("cria resultado de imagem com dados validos")
    void deveCriarResultadoDeImagem() {
        ResultadoExame resultado = ResultadoExame.criarImagem(exameId, pacienteId, "s3://bucket/raio-x.png",
                "Raio-X de torax", "Sem alteracoes significativas", "Exame de rotina");

        assertThat(resultado.getExameId()).isEqualTo(exameId);
        assertThat(resultado.getTipoResultado()).isEqualTo(TipoResultado.IMAGEM);
        assertThat(resultado.getArquivoUrl()).isEqualTo("s3://bucket/raio-x.png");
        assertThat(resultado.getItens()).isEmpty();
    }

    @Test
    @DisplayName("nao cria resultado de imagem sem referencia de arquivo")
    void naoDeveCriarImagemSemArquivo() {
        assertThatThrownBy(() -> ResultadoExame.criarImagem(exameId, pacienteId, "", "Descricao", "Laudo",
                "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ---------- criarLaboratorial ----------

    @Test
    @DisplayName("cria resultado laboratorial com um ou mais itens")
    void deveCriarResultadoLaboratorial() {
        List<ItemResultadoLaboratorial> itens = List.of(
                ItemResultadoLaboratorial.quantitativo("Hemoglobina", 14.2, "g/dL", 13.0, 17.0),
                ItemResultadoLaboratorial.qualitativo("HIV", "Nao reagente", SituacaoParametro.NORMAL));

        ResultadoExame resultado = ResultadoExame.criarLaboratorial(exameId, pacienteId, itens,
                "Exame de rotina");

        assertThat(resultado.getTipoResultado()).isEqualTo(TipoResultado.LABORATORIAL);
        assertThat(resultado.getItens()).hasSize(2);
    }

    @Test
    @DisplayName("nao cria resultado laboratorial sem nenhum item")
    void naoDeveCriarLaboratorialSemItens() {
        assertThatThrownBy(() -> ResultadoExame.criarLaboratorial(exameId, pacienteId, List.of(), "Observacao"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao cria resultado sem exame vinculado")
    void naoDeveCriarSemExameId() {
        assertThatThrownBy(() -> ResultadoExame.criarImagem(null, pacienteId, "url", "desc", "laudo", "obs"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}