package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/** Catalogo do que pode ser solicitado como exame, com instrucoes de preparo. */
public class TipoExame {

    private final UUID id;
    private String nome;
    private final CategoriaExame categoria;
    private String preparo;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private TipoExame(UUID id, String nome, CategoriaExame categoria, String preparo, boolean ativo,
                      Instant criadoEm, Instant atualizadoEm) {
        if (categoria == null) {
            throw new RegraDeNegocioException("A categoria do tipo de exame e obrigatoria.");
        }
        this.id = id;
        this.categoria = categoria;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        alterarDados(nome, preparo);
    }

    public static TipoExame criar(String nome, CategoriaExame categoria, String preparo) {
        Instant agora = Instant.now();
        return new TipoExame(UUID.randomUUID(), nome, categoria, preparo, true, agora, agora);
    }

    public static TipoExame reconstituir(UUID id, String nome, CategoriaExame categoria, String preparo,
                                         boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        return new TipoExame(id, nome, categoria, preparo, ativo, criadoEm, atualizadoEm);
    }

    public final void alterarDados(String novoNome, String novoPreparo) {
        if (novoNome == null || novoNome.trim().length() < 3) {
            throw new RegraDeNegocioException("O nome do tipo de exame deve ter no minimo 3 caracteres.");
        }
        this.nome = novoNome.trim();
        this.preparo = novoPreparo == null || novoPreparo.isBlank() ? null : novoPreparo.trim();
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Tipo de exame ja esta inativo.");
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
