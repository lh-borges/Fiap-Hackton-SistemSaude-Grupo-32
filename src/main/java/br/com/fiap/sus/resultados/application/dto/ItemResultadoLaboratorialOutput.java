package br.com.fiap.sus.resultados.application.dto;

import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import br.com.fiap.sus.resultados.domain.model.ItemResultadoLaboratorial;

public record ItemResultadoLaboratorialOutput(String nomeParametro, Double valor, String unidade,
                                              Double valorMinimoReferencia, Double valorMaximoReferencia,
                                              String resultadoTexto, SituacaoParametro situacao) {

    public static ItemResultadoLaboratorialOutput de(ItemResultadoLaboratorial item) {
        return new ItemResultadoLaboratorialOutput(item.getNomeParametro(), item.getValor(),
                item.getUnidade(), item.getValorMinimoReferencia(), item.getValorMaximoReferencia(),
                item.getResultadoTexto(), item.getSituacao());
    }
}