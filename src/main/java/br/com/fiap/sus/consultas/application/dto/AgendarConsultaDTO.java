package br.com.fiap.sus.consultas.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AgendarConsultaDTO(UUID pacienteId, UUID medicoId, UUID unidadeSaudeId,
                                 Instant dataHora, String motivo) {
}
