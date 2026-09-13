package br.com.fiap.sus.receitas.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RenovarReceitaDTO(UUID receitaOrigemId, UUID novaConsultaId, Instant novaValidade,
                                String novaObservacao) {
}