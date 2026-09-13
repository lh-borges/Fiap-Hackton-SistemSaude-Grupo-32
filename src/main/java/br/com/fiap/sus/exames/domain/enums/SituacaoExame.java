package br.com.fiap.sus.exames.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

public enum SituacaoExame {

    AGENDADO,
    REALIZADO,
    CANCELADO;

    public static SituacaoExame de(String valor) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Situacao invalida. Valores aceitos: AGENDADO, REALIZADO, CANCELADO."));
    }
}