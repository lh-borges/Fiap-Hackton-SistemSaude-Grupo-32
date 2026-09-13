package br.com.fiap.sus.resultados.presentation.response;

import br.com.fiap.sus.resultados.application.dto.ItemResultadoLaboratorialOutput;
import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;

public record ItemResultadoLaboratorialResponse(String nomeParametro, Double valor, String unidade,
                                                Double valorMinimoReferencia, Double valorMaximoReferencia,
                                                String resultadoTexto, SituacaoParametro situacao) {

    public static ItemResultadoLaboratorialResponse de(ItemResultadoLaboratorialOutput output) {
        return new ItemResultadoLaboratorialResponse(output.nomeParametro(), output.valor(),
                output.unidade(), output.valorMinimoReferencia(), output.valorMaximoReferencia(),
                output.resultadoTexto(), output.situacao());
    }
}