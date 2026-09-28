package br.com.fiap.sus.exames.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de uma solicitacao de exame para leitura por outros modulos (historico). Sem justificativa. */
public record SolicitacaoExameResumo(
        UUID id,
        UUID pacienteId,
        UUID medicoId,
        UUID tipoExameId,
        UUID consultaId,
        String situacao,
        Instant criadoEm
) {
}
