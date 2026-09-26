package br.com.fiap.sus.notificacoes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidade JPA da tabela not_notificacao. Nao e a entidade de dominio (Artigo II.3). */
@Entity
@Table(name = "not_notificacao")
public class NotificacaoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "evento_id", nullable = false, updatable = false)
    private UUID eventoId;

    @Column(name = "tipo", nullable = false, length = 40, updatable = false)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 150, updatable = false)
    private String titulo;

    @Column(name = "mensagem", nullable = false, length = 500, updatable = false)
    private String mensagem;

    @Column(name = "referencia_id", updatable = false)
    private UUID referenciaId;

    @Column(name = "lida", nullable = false)
    private boolean lida;

    @Column(name = "data_leitura")
    private Instant dataLeitura;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected NotificacaoEntity() {
    }

    public NotificacaoEntity(UUID id, UUID usuarioId, UUID eventoId, String tipo, String titulo, String mensagem,
                             UUID referenciaId, boolean lida, Instant dataLeitura, Instant criadoEm,
                             Instant atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.referenciaId = referenciaId;
        this.lida = lida;
        this.dataLeitura = dataLeitura;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** Unica mutacao permitida: a leitura (RN-04). */
    public void registrarLeitura(Instant dataLeitura, Instant atualizadoEm) {
        this.lida = true;
        this.dataLeitura = dataLeitura;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getEventoId() { return eventoId; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public UUID getReferenciaId() { return referenciaId; }
    public boolean isLida() { return lida; }
    public Instant getDataLeitura() { return dataLeitura; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
