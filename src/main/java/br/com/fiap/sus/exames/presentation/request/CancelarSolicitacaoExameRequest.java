package br.com.fiap.sus.exames.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CancelarSolicitacaoExameRequest(@NotBlank String motivo) {
}