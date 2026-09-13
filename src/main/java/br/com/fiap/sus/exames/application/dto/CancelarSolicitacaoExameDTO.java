package br.com.fiap.sus.exames.application.dto;

import java.util.UUID;

public record CancelarSolicitacaoExameDTO(UUID solicitacaoExameId, String motivo) {
}