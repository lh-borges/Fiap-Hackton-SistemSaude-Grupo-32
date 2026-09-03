package br.com.fiap.sus.shared.domain.vo;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

/**
 * CPF valido (RN-02 da feature 001). Guarda apenas os 11 digitos.
 * Nunca deve ser escrito em log (Artigo IV.6).
 */
public record Cpf(String valor) {

    public Cpf {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("CPF e obrigatorio.");
        }
        valor = valor.replaceAll("\\D", "");
        if (!ehValido(valor)) {
            throw new RegraDeNegocioException("CPF invalido.");
        }
    }

    public String formatado() {
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "." + valor.substring(6, 9)
                + "-" + valor.substring(9);
    }

    /** Mascara para exibicao em contextos nao sensiveis: 123.***.**9-00 nunca completo. */
    public String mascarado() {
        return "***." + valor.substring(3, 6) + ".***-**";
    }

    private static boolean ehValido(String digitos) {
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9, 10) == Character.getNumericValue(digitos.charAt(9))
                && digitoVerificador(digitos, 10, 11) == Character.getNumericValue(digitos.charAt(10));
    }

    private static int digitoVerificador(String digitos, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
