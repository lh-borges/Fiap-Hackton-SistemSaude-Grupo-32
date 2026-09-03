package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/** Local fisico de atendimento, identificado pelo CNES. */
public class UnidadeSaude {

    private final UUID id;
    private String nome;
    private final String cnes;
    private String telefone;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private UnidadeSaude(UUID id, String nome, String cnes, String telefone, boolean ativo,
                         Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.cnes = validarCnes(cnes);
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        alterarDados(nome, telefone);
    }

    public static UnidadeSaude criar(String nome, String cnes, String telefone) {
        Instant agora = Instant.now();
        return new UnidadeSaude(UUID.randomUUID(), nome, cnes, telefone, true, agora, agora);
    }

    public static UnidadeSaude reconstituir(UUID id, String nome, String cnes, String telefone,
                                            boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        return new UnidadeSaude(id, nome, cnes, telefone, ativo, criadoEm, atualizadoEm);
    }

    public final void alterarDados(String novoNome, String novoTelefone) {
        if (novoNome == null || novoNome.trim().length() < 3) {
            throw new RegraDeNegocioException("O nome da unidade deve ter no minimo 3 caracteres.");
        }
        this.nome = novoNome.trim();
        this.telefone = novoTelefone == null || novoTelefone.isBlank() ? null
                : novoTelefone.replaceAll("\\D", "");
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Unidade de saude ja esta inativa.");
        }
        this.ativo = false;
        this.atualizadoEm = Instant.now();
    }

    private static String validarCnes(String valor) {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        if (digitos.length() != 7) {
            throw new RegraDeNegocioException("O CNES deve ter 7 digitos.");
        }
        return digitos;
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
