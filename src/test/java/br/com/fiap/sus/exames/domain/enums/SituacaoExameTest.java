package br.com.fiap.sus.exames.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class SituacaoExameTest {

    @Test
    void deveConverterTextoValidoParaEnum() {
        assertThat(SituacaoExame.de("AGENDADO")).isEqualTo(SituacaoExame.AGENDADO);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThatThrownBy(() -> SituacaoExame.de("INEXISTENTE"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}