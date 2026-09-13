package br.com.fiap.sus.consultas.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RemarcarConsultaDTO(UUID consultaId, Instant novaDataHora) {
}