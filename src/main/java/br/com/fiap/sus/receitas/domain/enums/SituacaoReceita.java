package br.com.fiap.sus.receitas.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

/** VENCIDA nao e persistida: e derivada comparando validade com a data atual na leitura. */
public enum SituacaoReceita {

    ATIVA,
    CANCELADA,
    RENOVADA;

    public static SituacaoReceita de(String valor) {
        return Arrays.stream(values())
                .filter(situacao -> situacao.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Situacao invalida. Valores aceitos: ATIVA, CANCELADA, RENOVADA."));
    }
}