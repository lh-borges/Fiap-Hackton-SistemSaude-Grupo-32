package br.com.fiap.sus.receitas.infrastructure.persistence.entity;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rece_receita")
public class ReceitaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "consulta_id")
    private UUID consultaId;

    @Column(name = "data_emissao", nullable = false)
    private Instant dataEmissao;

    @Column(nullable = false)
    private Instant validade;

    @Column(length = 1000)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoReceita situacao;

    @Column(name = "receita_origem_id")
    private UUID receitaOrigemId;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    protected ReceitaEntity() {
    }

    public ReceitaEntity(UUID id, UUID pacienteId, UUID medicoId, UUID consultaId, Instant dataEmissao,
                         Instant validade, String observacao, SituacaoReceita situacao,
                         UUID receitaOrigemId, String motivoCancelamento) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.consultaId = consultaId;
        this.dataEmissao = dataEmissao;
        this.validade = validade;
        this.observacao = observacao;
        this.situacao = situacao;
        this.receitaOrigemId = receitaOrigemId;
        this.motivoCancelamento = motivoCancelamento;
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getConsultaId() { return consultaId; }
    public Instant getDataEmissao() { return dataEmissao; }
    public Instant getValidade() { return validade; }
    public String getObservacao() { return observacao; }
    public SituacaoReceita getSituacao() { return situacao; }
    public UUID getReceitaOrigemId() { return receitaOrigemId; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
}