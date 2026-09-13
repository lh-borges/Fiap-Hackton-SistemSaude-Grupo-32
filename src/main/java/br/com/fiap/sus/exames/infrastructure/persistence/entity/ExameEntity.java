package br.com.fiap.sus.exames.infrastructure.persistence.entity;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_exame")
public class ExameEntity {

    @Id
    private UUID id;

    @Column(name = "solicitacao_exame_id", nullable = false)
    private UUID solicitacaoExameId;

    @Column(name = "unidade_saude_id", nullable = false)
    private UUID unidadeSaudeId;

    @Column(name = "data_agendada", nullable = false)
    private Instant dataAgendada;

    @Column(name = "data_realizacao")
    private Instant dataRealizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoExame situacao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ExameEntity() {
    }

    public ExameEntity(UUID id, UUID solicitacaoExameId, UUID unidadeSaudeId, Instant dataAgendada,
                       Instant dataRealizacao, SituacaoExame situacao, Instant criadoEm,
                       Instant atualizadoEm) {
        this.id = id;
        this.solicitacaoExameId = solicitacaoExameId;
        this.unidadeSaudeId = unidadeSaudeId;
        this.dataAgendada = dataAgendada;
        this.dataRealizacao = dataRealizacao;
        this.situacao = situacao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() { return id; }
    public UUID getSolicitacaoExameId() { return solicitacaoExameId; }
    public UUID getUnidadeSaudeId() { return unidadeSaudeId; }
    public Instant getDataAgendada() { return dataAgendada; }
    public Instant getDataRealizacao() { return dataRealizacao; }
    public SituacaoExame getSituacao() { return situacao; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}