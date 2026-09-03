package br.com.fiap.sus.cadastros.api;

import java.util.UUID;

/** Visao minima do paciente para outros modulos. Sem cartao SUS nem dado clinico. */
public record PacienteResumo(UUID id, UUID usuarioId, boolean ativo) {
}
