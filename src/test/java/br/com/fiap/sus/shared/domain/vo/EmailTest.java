package br.com.fiap.sus.shared.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("E-mail")
class EmailTest {

    @Test
    @DisplayName("normaliza para minusculas e remove espacos")
    void normaliza() {
        assertThat(new Email("  Maria.Souza@SUS.gov.br ").valor()).isEqualTo("maria.souza@sus.gov.br");
    }

    @ParameterizedTest
    @DisplayName("rejeita formato invalido")
    @ValueSource(strings = {"sem-arroba", "@sus.gov.br", "maria@", "maria@sus", "maria @sus.gov.br"})
    void rejeitaFormatoInvalido(String valor) {
        assertThatThrownBy(() -> new Email(valor)).isInstanceOf(RegraDeNegocioException.class);
    }
}
