package br.com.fiap.sus.consultas.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de uma consulta para leitura por outros modulos (historico). Sem motivo nem observacoes. */
public record ConsultaResumo(
        UUID id,
        UUID pacienteId,
        UUID medicoId,
        UUID unidadeSaudeId,
        Instant dataHora,
        String situacao,
        boolean remarcada
) {
}
