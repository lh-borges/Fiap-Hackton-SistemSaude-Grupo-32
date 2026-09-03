package br.com.fiap.sus.cadastros.application.dto;

import java.util.UUID;

public record CadastrarMedicoInput(UUID usuarioId, String crm, String ufCrm, UUID especialidadeId) {
}
