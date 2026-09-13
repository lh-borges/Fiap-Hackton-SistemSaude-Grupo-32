package br.com.fiap.sus.exames.infrastructure.persistence.entity;

import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_solicitacao_exame")
public class SolicitacaoExameEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "tipo_exame_id", nullable = false)
    private UUID tipoExameId;

    @Column(name = "consulta_id")
    private UUID consultaId;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoSolicitacaoExame situacao;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected SolicitacaoExameEntity() {
    }

    public SolicitacaoExameEntity(UUID id, UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                  UUID consultaId, String justificativa, SituacaoSolicitacaoExame situacao,
                                  String motivoCancelamento, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.tipoExameId = tipoExameId;
        this.consultaId = consultaId;
        this.justificativa = justificativa;
        this.situacao = situacao;
        this.motivoCancelamento = motivoCancelamento;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getTipoExameId() { return tipoExameId; }
    public UUID getConsultaId() { return consultaId; }
    public String getJustificativa() { return justificativa; }
    public SituacaoSolicitacaoExame getSituacao() { return situacao; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}