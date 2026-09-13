package br.com.fiap.sus.consultas.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.Arrays;

/** RN-03. "Confirmada" tem uso pendente de esclarecimento na spec (ver secao 13). */
public enum SituacaoConsulta {

    AGENDADA,
    CONFIRMADA,
    REALIZADA,
    CANCELADA;

    public static SituacaoConsulta de(String valor) {
        return Arrays.stream(values())
                .filter(situacao -> situacao.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Situacao invalida. Valores aceitos: AGENDADA, CONFIRMADA, REALIZADA, CANCELADA."));
    }
}