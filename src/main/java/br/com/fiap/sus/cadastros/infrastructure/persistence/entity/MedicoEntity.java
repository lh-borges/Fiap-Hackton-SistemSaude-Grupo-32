package br.com.fiap.sus.cadastros.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cad_medico")
public class MedicoEntity {

    @Id
    private UUID id;

    /** Referencia logica ao modulo iam: sem FK, por decisao arquitetural (Artigo III.3). */
    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    @Column(nullable = false, length = 15)
    private String crm;

    @Column(name = "uf_crm", nullable = false, length = 2)
    private String ufCrm;

    @Column(name = "especialidade_id", nullable = false)
    private UUID especialidadeId;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected MedicoEntity() {
    }

    public MedicoEntity(UUID id, UUID usuarioId, String crm, String ufCrm, UUID especialidadeId,
                        boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.crm = crm;
        this.ufCrm = ufCrm;
        this.especialidadeId = especialidadeId;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getCrm() {
        return crm;
    }

    public String getUfCrm() {
        return ufCrm;
    }

    public UUID getEspecialidadeId() {
        return especialidadeId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
