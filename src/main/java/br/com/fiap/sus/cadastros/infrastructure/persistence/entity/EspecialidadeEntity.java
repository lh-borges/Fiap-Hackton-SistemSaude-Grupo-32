package br.com.fiap.sus.cadastros.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cad_especialidade")
public class EspecialidadeEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected EspecialidadeEntity() {
    }

    public EspecialidadeEntity(UUID id, String nome, String descricao, boolean ativo,
                               Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
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
