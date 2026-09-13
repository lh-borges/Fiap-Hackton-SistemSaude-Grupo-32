package br.com.fiap.sus.consultas.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CancelarConsultaRequest(@NotBlank String motivoCancelamento) {
}