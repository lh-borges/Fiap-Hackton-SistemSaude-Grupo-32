package br.com.fiap.sus.notificacoes.domain.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Marcador de idempotencia do consumidor (Artigo VI.3, RF-06). Um evento ja registrado
 * aqui nunca gera notificacao de novo, mesmo que seja reentregue.
 */
public interface EventoProcessadoRepository {

    boolean jaProcessado(UUID eventoId);

    /**
     * Registra o processamento. Deve ser chamado na mesma transacao em que as notificacoes
     * sao gravadas; a chave primaria em {@code eventoId} e a defesa contra corrida.
     */
    void registrar(UUID eventoId, String tipoEvento, Instant processadoEm);
}
