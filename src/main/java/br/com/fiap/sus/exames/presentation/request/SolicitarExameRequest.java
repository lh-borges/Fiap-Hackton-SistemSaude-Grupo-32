package br.com.fiap.sus.exames.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SolicitarExameRequest(
        @NotNull UUID pacienteId,
        @NotNull UUID tipoExameId,
        UUID consultaId,
        @NotBlank String justificativa) {
}