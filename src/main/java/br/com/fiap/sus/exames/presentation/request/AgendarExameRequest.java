package br.com.fiap.sus.exames.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record AgendarExameRequest(@NotNull UUID unidadeSaudeId, @NotNull @Future Instant dataAgendada) {
}