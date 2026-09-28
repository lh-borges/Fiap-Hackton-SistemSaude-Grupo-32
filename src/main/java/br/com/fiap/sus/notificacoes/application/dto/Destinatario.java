package br.com.fiap.sus.notificacoes.application.dto;

import java.util.UUID;

/**
 * Destinatario de um fato. {@code usuarioId} nulo significa que o paciente ou medico do
 * evento nao tem usuario vinculado: o processador descarta so esse destinatario (RN-10).
 */
public record Destinatario(PapelDestinatario papel, UUID usuarioId) {

    public boolean resolvido() {
        return usuarioId != null;
    }
}
