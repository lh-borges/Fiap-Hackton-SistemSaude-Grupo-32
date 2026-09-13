package br.com.fiap.sus.consultas.application.dto;

import java.util.UUID;

public record CancelarConsultaDTO(UUID consultaId, String motivoCancelamento) {
}