package br.com.fiap.sus.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope comum dos eventos de dominio (Artigo VI e {@code specs/000-plataforma-sus/events.md}).
 *
 * <p>{@code eventoId} e gerado a cada publicacao e e a chave de deduplicacao do consumidor;
 * {@code ocorridoEm} e o instante UTC do fato. O payload nunca carrega conteudo clinico.
 */
public interface EventoDominio {

    UUID eventoId();

    Instant ocorridoEm();
}
