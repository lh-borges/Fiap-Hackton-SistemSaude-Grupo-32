package br.com.fiap.sus.consultas.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record AgendarConsultaRequest(
        @NotNull UUID pacienteId,
        @NotNull UUID medicoId,
        @NotNull UUID unidadeSaudeId,
        @NotNull @Future Instant dataHora,
        @NotBlank String motivo) {
}