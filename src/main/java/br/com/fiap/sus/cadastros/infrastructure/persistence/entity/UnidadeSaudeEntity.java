package br.com.fiap.sus.cadastros.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cad_unidade_saude")
public class UnidadeSaudeEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 7, unique = true)
    private String cnes;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected UnidadeSaudeEntity() {
    }

    public UnidadeSaudeEntity(UUID id, String nome, String cnes, String telefone, boolean ativo,
                              Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.cnes = cnes;
        this.telefone = telefone;
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

    public String getCnes() {
        return cnes;
    }

    public String getTelefone() {
        return telefone;
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
