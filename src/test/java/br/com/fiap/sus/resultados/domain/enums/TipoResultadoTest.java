package br.com.fiap.sus.resultados.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class TipoResultadoTest {

    @Test
    void deveConverterTextoValidoParaEnum() {
        assertThat(TipoResultado.de("IMAGEM")).isEqualTo(TipoResultado.IMAGEM);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThatThrownBy(() -> TipoResultado.de("INEXISTENTE"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}