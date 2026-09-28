package br.com.fiap.sus.exames.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de um exame (agendado ou realizado) para leitura por outros modulos (historico). */
public record ExameResumo(
        UUID id,
        UUID solicitacaoExameId,
        UUID pacienteId,
        UUID medicoSolicitanteId,
        UUID unidadeSaudeId,
        Instant dataAgendada,
        Instant dataRealizacao,
        String situacao
) {
}
