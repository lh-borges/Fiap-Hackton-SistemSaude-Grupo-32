package br.com.fiap.sus.cadastros.infrastructure.persistence.entity;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cad_tipo_exame")
public class TipoExameEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CategoriaExame categoria;

    @Column(columnDefinition = "text")
    private String preparo;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected TipoExameEntity() {
    }

    public TipoExameEntity(UUID id, String nome, CategoriaExame categoria, String preparo, boolean ativo,
                           Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.preparo = preparo;
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

    public CategoriaExame getCategoria() {
        return categoria;
    }

    public String getPreparo() {
        return preparo;
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
