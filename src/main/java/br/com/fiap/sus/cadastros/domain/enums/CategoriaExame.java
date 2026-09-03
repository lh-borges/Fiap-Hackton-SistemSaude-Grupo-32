package br.com.fiap.sus.cadastros.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

/**
 * Define se o tipo de exame produz resultado de imagem ou laboratorial.
 * O modulo de resultados usa esta informacao para validar a coerencia do resultado
 * registrado (feature 005, RN-02).
 */
public enum CategoriaExame {

    IMAGEM,
    LABORATORIAL;

    public static CategoriaExame de(String valor) {
        return Arrays.stream(values())
                .filter(categoria -> categoria.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Categoria invalida. Valores aceitos: IMAGEM, LABORATORIAL."));
    }
}
