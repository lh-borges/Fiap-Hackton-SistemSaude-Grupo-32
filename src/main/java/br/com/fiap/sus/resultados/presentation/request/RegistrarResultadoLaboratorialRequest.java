package br.com.fiap.sus.resultados.presentation.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RegistrarResultadoLaboratorialRequest(
        @NotNull UUID exameId,
        @NotNull UUID pacienteId,
        @NotEmpty List<ItemResultadoLaboratorialRequest> itens,
        String observacao) {
}