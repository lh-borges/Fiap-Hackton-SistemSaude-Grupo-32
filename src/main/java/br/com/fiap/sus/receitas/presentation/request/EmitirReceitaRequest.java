package br.com.fiap.sus.receitas.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmitirReceitaRequest(
        @NotNull UUID pacienteId,
        UUID consultaId,
        @NotEmpty List<ItemReceitaRequest> itens,
        @NotNull @Future Instant validade,
        String observacao) {
}