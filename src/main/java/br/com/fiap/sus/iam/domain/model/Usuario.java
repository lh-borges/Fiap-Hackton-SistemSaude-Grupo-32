package br.com.fiap.sus.iam.domain.model;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Identidade de acesso. Entidade de dominio pura: nao conhece JPA nem Spring (Artigo II.2).
 * A exclusao e sempre logica (RN-05).
 */
public class Usuario {

    private final UUID id;
    private String nome;
    private final Cpf cpf;
    private Email email;
    private String senhaHash;
    private boolean ativo;
    private Set<RoleNome> roles;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Usuario(UUID id, String nome, Cpf cpf, Email email, String senhaHash, boolean ativo,
                    Set<RoleNome> roles, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.cpf = cpf;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        definirNome(nome);
        definirEmail(email);
        definirSenhaHash(senhaHash);
        definirRoles(roles);
    }

    public static Usuario criar(String nome, Cpf cpf, Email email, String senhaHash, Set<RoleNome> roles) {
        Instant agora = Instant.now();
        return new Usuario(UUID.randomUUID(), nome, cpf, email, senhaHash, true, roles, agora, agora);
    }

    public static Usuario reconstituir(UUID id, String nome, Cpf cpf, Email email, String senhaHash,
                                       boolean ativo, Set<RoleNome> roles, Instant criadoEm, Instant atualizadoEm) {
        return new Usuario(id, nome, cpf, email, senhaHash, ativo, roles, criadoEm, atualizadoEm);
    }

    public void atualizarDados(String novoNome, Email novoEmail) {
        definirNome(novoNome);
        definirEmail(novoEmail);
        marcarAtualizacao();
    }

    public void alterarSenha(String novoHash) {
        definirSenhaHash(novoHash);
        marcarAtualizacao();
    }

    public void substituirRoles(Set<RoleNome> novasRoles) {
        definirRoles(novasRoles);
        marcarAtualizacao();
    }

    /** RN-05: usuario nunca e excluido fisicamente. */
    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Usuario ja esta inativo.");
        }
        this.ativo = false;
        marcarAtualizacao();
    }

    public void ativar() {
        this.ativo = true;
        marcarAtualizacao();
    }

    public boolean temRole(RoleNome role) {
        return roles.contains(role);
    }

    private void definirNome(String valor) {
        if (valor == null || valor.trim().length() < 3) {
            throw new RegraDeNegocioException("O nome deve ter no minimo 3 caracteres.");
        }
        this.nome = valor.trim();
    }

    private void definirEmail(Email valor) {
        if (valor == null) {
            throw new RegraDeNegocioException("E-mail e obrigatorio.");
        }
        this.email = valor;
    }

    private void definirSenhaHash(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("A senha e obrigatoria.");
        }
        this.senhaHash = valor;
    }

    /** RN-04: todo usuario tem ao menos um perfil. */
    private void definirRoles(Set<RoleNome> valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RegraDeNegocioException("O usuario deve possuir ao menos um perfil.");
        }
        this.roles = EnumSet.copyOf(valor);
    }

    private void marcarAtualizacao() {
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public Email getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<RoleNome> getRoles() {
        return Set.copyOf(roles);
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
