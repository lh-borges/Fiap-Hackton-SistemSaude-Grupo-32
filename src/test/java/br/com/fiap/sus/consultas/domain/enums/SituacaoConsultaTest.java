package br.com.fiap.sus.consultas.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

class SituacaoConsultaTest {

    @Test
    void deveConverterTextoValidoParaEnum() {
        assertThat(SituacaoConsulta.de("AGENDADA")).isEqualTo(SituacaoConsulta.AGENDADA);
        assertThat(SituacaoConsulta.de("cancelada")).isEqualTo(SituacaoConsulta.CANCELADA);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThatThrownBy(() -> SituacaoConsulta.de("INEXISTENTE"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}