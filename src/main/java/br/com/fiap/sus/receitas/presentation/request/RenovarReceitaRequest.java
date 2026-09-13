package br.com.fiap.sus.receitas.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record RenovarReceitaRequest(UUID novaConsultaId, @NotNull @Future Instant novaValidade,
                                    String novaObservacao) {
}