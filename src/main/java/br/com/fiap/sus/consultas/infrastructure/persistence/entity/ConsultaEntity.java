package br.com.fiap.sus.consultas.infrastructure.persistence.entity;

import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cons_consulta")
public class ConsultaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "unidade_saude_id", nullable = false)
    private UUID unidadeSaudeId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoConsulta situacao;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(length = 2000)
    private String observacoes;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(nullable = false)
    private boolean remarcada;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ConsultaEntity() {
    }

    public ConsultaEntity(UUID id, UUID pacienteId, UUID medicoId, UUID unidadeSaudeId, Instant dataHora,
                          SituacaoConsulta situacao, String motivo, String observacoes,
                          String motivoCancelamento, boolean remarcada, Instant criadoEm,
                          Instant atualizadoEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.unidadeSaudeId = unidadeSaudeId;
        this.dataHora = dataHora;
        this.situacao = situacao;
        this.motivo = motivo;
        this.observacoes = observacoes;
        this.motivoCancelamento = motivoCancelamento;
        this.remarcada = remarcada;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getUnidadeSaudeId() { return unidadeSaudeId; }
    public Instant getDataHora() { return dataHora; }
    public SituacaoConsulta getSituacao() { return situacao; }
    public String getMotivo() { return motivo; }
    public String getObservacoes() { return observacoes; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public boolean isRemarcada() { return remarcada; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}