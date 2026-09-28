package br.com.fiap.sus.notificacoes.domain.repository;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

import java.util.UUID;

/**
 * Filtro de listagem. O {@code usuarioId} e obrigatorio porque toda listagem e do proprio
 * usuario (HU-02/HU-03); {@code lida} nulo devolve todas.
 */
public record NotificacaoFiltro(UUID usuarioId, Boolean lida) {

    public NotificacaoFiltro {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("A listagem de notificacoes exige o usuario destinatario.");
        }
    }

    public static NotificacaoFiltro todas(UUID usuarioId) {
        return new NotificacaoFiltro(usuarioId, null);
    }
}
