package br.com.fiap.sus.receitas.application.dto;

import java.util.UUID;

public record CancelarReceitaDTO(UUID receitaId, String motivo) {
}