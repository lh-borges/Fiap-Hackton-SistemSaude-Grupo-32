package br.com.fiap.sus.consultas.application.dto;

import java.util.UUID;

public record RegistrarRealizacaoConsultaDTO(UUID consultaId, String observacoes) {
}