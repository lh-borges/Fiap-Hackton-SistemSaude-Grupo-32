package br.com.fiap.sus.pareceres.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "par_parecer_medico")
public class ParecerMedicoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "resultado_exame_id", nullable = false, updatable = false)
    private UUID resultadoExameId;

    @Column(name = "paciente_id", nullable = false, updatable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false, updatable = false)
    private UUID medicoId;

    @Column(nullable = false, length = 5000, updatable = false)
    private String descricao;

    @Column(name = "data_parecer", nullable = false, updatable = false)
    private Instant dataParecer;

    @Column(name = "criado_por_usuario_id", updatable = false)
    private UUID criadoPorUsuarioId;

    public UUID getCriadoPorUsuarioId() { return criadoPorUsuarioId; }

    protected ParecerMedicoEntity() {
    }

    public ParecerMedicoEntity(
            UUID id,
            UUID resultadoExameId,
            UUID pacienteId,
            UUID medicoId,
            String descricao,
            Instant dataParecer,
            UUID criadoPorUsuarioId
    ) {
        this.criadoPorUsuarioId = criadoPorUsuarioId;
        this.id = id;
        this.resultadoExameId = resultadoExameId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.descricao = descricao;
        this.dataParecer = dataParecer;
    }

    public UUID getId() { return id; }
    public UUID getResultadoExameId() { return resultadoExameId; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public String getDescricao() { return descricao; }
    public Instant getDataParecer() { return dataParecer; }
}
