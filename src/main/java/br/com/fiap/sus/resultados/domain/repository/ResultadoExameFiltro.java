package br.com.fiap.sus.resultados.domain.repository;

import java.time.Instant;
import java.util.UUID;

public record ResultadoExameFiltro(UUID pacienteId, Instant periodoInicio, Instant periodoFim) {
}