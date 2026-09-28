package br.com.fiap.sus.receitas.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de uma receita para leitura por outros modulos (historico). Sem itens nem observacao. */
public record ReceitaResumo(
        UUID id,
        UUID pacienteId,
        UUID medicoId,
        UUID consultaId,
        Instant dataEmissao,
        Instant validade,
        String situacao,
        UUID receitaOrigemId
) {
}
