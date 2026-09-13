package br.com.fiap.sus.resultados.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

/** RN-02: deve corresponder a categoria do tipo de exame solicitado. RN-04: nunca os dois. */
public enum TipoResultado {

    IMAGEM,
    LABORATORIAL;

    public static TipoResultado de(String valor) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Tipo invalido. Valores aceitos: IMAGEM, LABORATORIAL."));
    }
}