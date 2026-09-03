package br.com.fiap.sus.shared.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CPF (RN-02 da feature 001)")
class CpfTest {

    @Test
    @DisplayName("aceita CPF valido e remove a formatacao")
    void aceitaCpfValido() {
        Cpf cpf = new Cpf("111.444.777-35");

        assertThat(cpf.valor()).isEqualTo("11144477735");
        assertThat(cpf.formatado()).isEqualTo("111.444.777-35");
    }

    @ParameterizedTest
    @DisplayName("rejeita CPF com digito verificador incorreto, tamanho errado ou digitos repetidos")
    @ValueSource(strings = {"11144477736", "1114447773", "111444777355", "11111111111", "abcdefghijk"})
    void rejeitaCpfInvalido(String valor) {
        assertThatThrownBy(() -> new Cpf(valor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("rejeita CPF ausente")
    void rejeitaCpfAusente() {
        assertThatThrownBy(() -> new Cpf(null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Cpf("  ")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("mascara nao expoe o CPF completo")
    void mascaraNaoExpoeCpfCompleto() {
        assertThat(new Cpf("11144477735").mascarado()).doesNotContain("11144477735");
    }
}
