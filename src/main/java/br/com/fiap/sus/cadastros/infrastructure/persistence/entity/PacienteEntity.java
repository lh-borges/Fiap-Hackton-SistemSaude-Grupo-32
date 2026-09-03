package br.com.fiap.sus.cadastros.infrastructure.persistence.entity;

import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cad_paciente")
public class PacienteEntity {

    @Id
    private UUID id;

    /** Referencia logica ao modulo iam: sem FK, por decisao arquitetural (Artigo III.3). */
    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    @Column(name = "cartao_sus", nullable = false, length = 15, unique = true)
    private String cartaoSus;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Sexo sexo;

    @Column(name = "tipo_sanguineo", length = 3)
    private String tipoSanguineo;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PacienteEntity() {
    }

    public PacienteEntity(UUID id, UUID usuarioId, String cartaoSus, LocalDate dataNascimento, Sexo sexo,
                          String tipoSanguineo, boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.cartaoSus = cartaoSus;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.tipoSanguineo = tipoSanguineo;
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

    public String getCartaoSus() {
        return cartaoSus;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
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
