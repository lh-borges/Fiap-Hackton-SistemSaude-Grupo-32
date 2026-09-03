package br.com.fiap.sus.iam.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

/**
 * Senha ainda nao codificada. Existe apenas para aplicar a RN-03 (minimo de 8 caracteres,
 * com letra e numero) antes de gerar o hash. Nunca e persistida nem registrada em log.
 */
public record SenhaEmTexto(String valor) {

    public SenhaEmTexto {
        if (valor == null || valor.length() < 8) {
            throw new RegraDeNegocioException("A senha deve ter no minimo 8 caracteres.");
        }
        boolean temLetra = valor.chars().anyMatch(Character::isLetter);
        boolean temNumero = valor.chars().anyMatch(Character::isDigit);
        if (!temLetra || !temNumero) {
            throw new RegraDeNegocioException("A senha deve conter ao menos uma letra e um numero.");
        }
    }

    @Override
    public String toString() {
        return "SenhaEmTexto[protegida]";
    }
}
