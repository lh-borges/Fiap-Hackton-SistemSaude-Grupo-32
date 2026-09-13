package br.com.fiap.sus.receitas.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ItemReceitaRequest(
        @NotBlank String medicamento,
        @NotBlank String dosagem,
        @NotBlank String frequencia,
        @NotBlank String duracao,
        String orientacao) {
}