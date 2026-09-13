package br.com.fiap.sus.exames.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class SituacaoSolicitacaoExameTest {

    @Test
    void deveConverterTextoValidoParaEnum() {
        assertThat(SituacaoSolicitacaoExame.de("PENDENTE")).isEqualTo(SituacaoSolicitacaoExame.PENDENTE);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThatThrownBy(() -> SituacaoSolicitacaoExame.de("INEXISTENTE"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}