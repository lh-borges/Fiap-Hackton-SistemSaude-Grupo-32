package br.com.fiap.sus.cadastros.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

public enum Sexo {

    MASCULINO,
    FEMININO,
    OUTRO,
    NAO_INFORMADO;

    public static Sexo de(String valor) {
        return Arrays.stream(values())
                .filter(sexo -> sexo.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Sexo invalido. Valores aceitos: MASCULINO, FEMININO, OUTRO, NAO_INFORMADO."));
    }
}
