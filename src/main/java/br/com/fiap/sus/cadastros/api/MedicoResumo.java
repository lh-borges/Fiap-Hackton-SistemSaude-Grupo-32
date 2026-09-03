package br.com.fiap.sus.cadastros.api;

import java.util.UUID;

public record MedicoResumo(UUID id, UUID usuarioId, String crm, String ufCrm, UUID especialidadeId,
                           boolean ativo) {
}
