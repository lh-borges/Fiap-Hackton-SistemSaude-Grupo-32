package br.com.fiap.sus.notificacoes.application.dto;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;

import java.time.Instant;
import java.util.UUID;

/** Saida dos casos de uso de leitura. Nao expoe usuarioId nem eventoId. */
public record NotificacaoOutput(UUID id, TipoNotificacao tipo, String titulo, String mensagem, UUID referenciaId,
                                String recurso, boolean lida, Instant dataLeitura, Instant criadoEm) {

    public static NotificacaoOutput de(Notificacao n) {
        return new NotificacaoOutput(n.getId(), n.getTipo(), n.getTitulo(), n.getMensagem(), n.getReferenciaId(),
                n.getTipo().getRecurso(), n.isLida(), n.getDataLeitura(), n.getCriadoEm());
    }
}
