package br.com.fiap.sus.shared.domain.vo;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.regex.Pattern;

/** Endereco de e-mail normalizado em minusculas. */
public record Email(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("E-mail e obrigatorio.");
        }
        valor = valor.trim().toLowerCase();
        if (!FORMATO.matcher(valor).matches()) {
            throw new RegraDeNegocioException("E-mail invalido.");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
