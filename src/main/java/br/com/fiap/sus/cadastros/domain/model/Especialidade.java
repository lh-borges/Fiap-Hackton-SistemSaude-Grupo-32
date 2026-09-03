package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/** Area de atuacao medica. */
public class Especialidade {

    private final UUID id;
    private String nome;
    private String descricao;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Especialidade(UUID id, String nome, String descricao, boolean ativo,
                          Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        alterarDados(nome, descricao);
    }

    public static Especialidade criar(String nome, String descricao) {
        Instant agora = Instant.now();
        return new Especialidade(UUID.randomUUID(), nome, descricao, true, agora, agora);
    }

    public static Especialidade reconstituir(UUID id, String nome, String descricao, boolean ativo,
                                             Instant criadoEm, Instant atualizadoEm) {
        return new Especialidade(id, nome, descricao, ativo, criadoEm, atualizadoEm);
    }

    public final void alterarDados(String novoNome, String novaDescricao) {
        if (novoNome == null || novoNome.trim().length() < 3) {
            throw new RegraDeNegocioException("O nome da especialidade deve ter no minimo 3 caracteres.");
        }
        this.nome = novoNome.trim();
        this.descricao = novaDescricao == null ? null : novaDescricao.trim();
        this.atualizadoEm = Instant.now();
    }

    /** RN-07: item de catalogo em uso nunca e excluido, apenas inativado. */
    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Especialidade ja esta inativa.");
        }
        this.ativo = false;
        this.atualizadoEm = Instant.now();
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
