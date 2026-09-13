package br.com.fiap.sus.receitas.presentation.response;

import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReceitaResponse(UUID id, UUID pacienteId, UUID medicoId, UUID consultaId,
                              List<ItemReceitaResponse> itens, Instant dataEmissao, Instant validade,
                              String observacao, SituacaoReceita situacao, boolean vencida,
                              UUID receitaOrigemId, String motivoCancelamento) {

    public static ReceitaResponse de(ReceitaOutput output) {
        List<ItemReceitaResponse> itens = output.itens().stream().map(ItemReceitaResponse::de).toList();
        return new ReceitaResponse(output.id(), output.pacienteId(), output.medicoId(), output.consultaId(),
                itens, output.dataEmissao(), output.validade(), output.observacao(), output.situacao(),
                output.vencida(), output.receitaOrigemId(), output.motivoCancelamento());
    }
}