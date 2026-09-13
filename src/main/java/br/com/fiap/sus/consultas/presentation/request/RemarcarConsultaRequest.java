package br.com.fiap.sus.consultas.presentation.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record RemarcarConsultaRequest(@NotNull @Future Instant novaDataHora) {
}