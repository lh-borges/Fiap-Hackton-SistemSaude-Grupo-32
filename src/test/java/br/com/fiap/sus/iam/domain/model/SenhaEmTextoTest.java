package br.com.fiap.sus.iam.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Senha em texto (RN-03)")
class SenhaEmTextoTest {

    @Test
    @DisplayName("aceita senha com letra, numero e 8 caracteres")
    void aceitaSenhaForte() {
        assertThat(new SenhaEmTexto("Senha123").valor()).isEqualTo("Senha123");
    }

    @ParameterizedTest
    @DisplayName("rejeita senha curta, so com letras ou so com numeros")
    @ValueSource(strings = {"Abc123", "somenteletras", "12345678"})
    void rejeitaSenhaFraca(String valor) {
        assertThatThrownBy(() -> new SenhaEmTexto(valor)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("nao expoe a senha no toString")
    void naoExpoeNoToString() {
        assertThat(new SenhaEmTexto("Senha123").toString()).doesNotContain("Senha123");
    }
}
