package br.com.fiap.sus.exames.application.dto;

import java.util.UUID;

public record SolicitarExameDTO(UUID pacienteId, UUID tipoExameId, UUID consultaId, String justificativa) {
}