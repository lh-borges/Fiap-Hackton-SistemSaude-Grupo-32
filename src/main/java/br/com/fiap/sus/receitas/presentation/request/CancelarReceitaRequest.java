package br.com.fiap.sus.receitas.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CancelarReceitaRequest(@NotBlank String motivo) {
}