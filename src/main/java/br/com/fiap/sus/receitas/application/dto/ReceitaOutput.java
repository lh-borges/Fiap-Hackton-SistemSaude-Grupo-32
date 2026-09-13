package br.com.fiap.sus.receitas.application.dto;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReceitaOutput(UUID id, UUID pacienteId, UUID medicoId, UUID consultaId,
                            List<ItemReceitaOutput> itens, Instant dataEmissao, Instant validade,
                            String observacao, SituacaoReceita situacao, boolean vencida,
                            UUID receitaOrigemId, String motivoCancelamento) {

    public static ReceitaOutput de(Receita r) {
        List<ItemReceitaOutput> itens = r.getItens().stream().map(ItemReceitaOutput::de).toList();
        return new ReceitaOutput(r.getId(), r.getPacienteId(), r.getMedicoId(), r.getConsultaId(), itens,
                r.getDataEmissao(), r.getValidade(), r.getObservacao(), r.getSituacao(), r.estaVencida(),
                r.getReceitaOrigemId(), r.getMotivoCancelamento());
    }
}