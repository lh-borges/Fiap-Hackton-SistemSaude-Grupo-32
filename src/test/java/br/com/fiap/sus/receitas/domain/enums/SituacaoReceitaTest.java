package br.com.fiap.sus.receitas.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class SituacaoReceitaTest {

    @Test
    void deveConverterTextoValidoParaEnum() {
        assertThat(SituacaoReceita.de("ATIVA")).isEqualTo(SituacaoReceita.ATIVA);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThatThrownBy(() -> SituacaoReceita.de("INEXISTENTE"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}