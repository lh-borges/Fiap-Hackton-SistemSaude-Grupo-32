package br.com.fiap.sus.resultados.application.dto;

import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResultadoExameOutput(UUID id, UUID exameId, UUID pacienteId, TipoResultado tipoResultado,
                                   Instant dataResultado, String observacao, String arquivoUrl,
                                   String descricao, String laudo,
                                   List<ItemResultadoLaboratorialOutput> itens, boolean possuiParecer) {

    public static ResultadoExameOutput de(ResultadoExame r) {
        return de(r, false);
    }

    public static ResultadoExameOutput de(ResultadoExame r, boolean possuiParecer) {
        List<ItemResultadoLaboratorialOutput> itens = r.getItens().stream()
                .map(ItemResultadoLaboratorialOutput::de)
                .toList();
        return new ResultadoExameOutput(r.getId(), r.getExameId(), r.getPacienteId(), r.getTipoResultado(),
                r.getDataResultado(), r.getObservacao(), r.getArquivoUrl(), r.getDescricao(), r.getLaudo(),
                itens, possuiParecer);
    }
}
