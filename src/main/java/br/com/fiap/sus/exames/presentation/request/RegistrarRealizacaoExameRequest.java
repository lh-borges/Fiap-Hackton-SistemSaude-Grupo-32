package br.com.fiap.sus.exames.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record RegistrarRealizacaoExameRequest(@NotNull @PastOrPresent Instant dataRealizacao) {
}