package br.com.fiap.sus.iam.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "iam_usuario")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "iam_usuario_role",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(UUID id, String nome, String cpf, String email, String senha, boolean ativo,
                         Set<RoleEntity> roles, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.ativo = ativo;
        this.roles = new LinkedHashSet<>(roles);
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
