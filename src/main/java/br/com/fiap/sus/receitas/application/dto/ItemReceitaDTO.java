package br.com.fiap.sus.receitas.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItemReceitaDTO(String medicamento, String dosagem, String frequencia,
                             String duracao, String orientacao) {
}