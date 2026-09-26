package br.com.fiap.sus.notificacoes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Marcador de idempotencia do consumidor (tabela not_evento_processado). */
@Entity
@Table(name = "not_evento_processado")
public class EventoProcessadoEntity {

    @Id
    @Column(name = "evento_id", nullable = false, updatable = false)
    private UUID eventoId;

    @Column(name = "tipo_evento", nullable = false, length = 80, updatable = false)
    private String tipoEvento;

    @Column(name = "processado_em", nullable = false, updatable = false)
    private Instant processadoEm;

    protected EventoProcessadoEntity() {
    }

    public EventoProcessadoEntity(UUID eventoId, String tipoEvento, Instant processadoEm) {
        this.eventoId = eventoId;
        this.tipoEvento = tipoEvento;
        this.processadoEm = processadoEm;
    }

    public UUID getEventoId() { return eventoId; }
    public String getTipoEvento() { return tipoEvento; }
    public Instant getProcessadoEm() { return processadoEm; }
}
