package br.com.fiap.sus.resultados.presentation.response;

import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResultadoExameResponse(UUID id, UUID exameId, UUID pacienteId, TipoResultado tipoResultado,
                                     Instant dataResultado, String observacao, String arquivoUrl,
                                     String descricao, String laudo,
                                     List<ItemResultadoLaboratorialResponse> itens, boolean possuiParecer) {

    public static ResultadoExameResponse de(ResultadoExameOutput output) {
        List<ItemResultadoLaboratorialResponse> itens = output.itens().stream()
                .map(ItemResultadoLaboratorialResponse::de)
                .toList();
        return new ResultadoExameResponse(output.id(), output.exameId(), output.pacienteId(),
                output.tipoResultado(), output.dataResultado(), output.observacao(), output.arquivoUrl(),
                output.descricao(), output.laudo(), itens, output.possuiParecer());
    }
}
