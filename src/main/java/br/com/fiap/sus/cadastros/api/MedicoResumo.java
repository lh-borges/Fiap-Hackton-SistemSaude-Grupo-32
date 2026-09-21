package br.com.fiap.sus.cadastros.api;

import java.util.UUID;

public record MedicoResumo(UUID id, UUID usuarioId, String nome, String email, String crm, String ufCrm,
                           UUID especialidadeId, String especialidade, boolean ativo) {
}
