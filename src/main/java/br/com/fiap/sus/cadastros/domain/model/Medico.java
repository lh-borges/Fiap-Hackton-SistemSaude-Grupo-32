package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Dados de dominio do medico. CRM e unico dentro da UF (RN-03). */
public class Medico {

    private static final Set<String> UFS = Set.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
            "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC",
            "SE", "SP", "TO");

    private final UUID id;
    private final UUID usuarioId;
    private final String crm;
    private final String ufCrm;
    private UUID especialidadeId;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Medico(UUID id, UUID usuarioId, String crm, String ufCrm, UUID especialidadeId, boolean ativo,
                   Instant criadoEm, Instant atualizadoEm) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("O medico deve estar vinculado a um usuario.");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.crm = validarCrm(crm);
        this.ufCrm = validarUf(ufCrm);
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        alterarEspecialidade(especialidadeId);
    }

    public static Medico criar(UUID usuarioId, String crm, String ufCrm, UUID especialidadeId) {
        Instant agora = Instant.now();
        return new Medico(UUID.randomUUID(), usuarioId, crm, ufCrm, especialidadeId, true, agora, agora);
    }

    public static Medico reconstituir(UUID id, UUID usuarioId, String crm, String ufCrm, UUID especialidadeId,
                                      boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        return new Medico(id, usuarioId, crm, ufCrm, especialidadeId, ativo, criadoEm, atualizadoEm);
    }

    public final void alterarEspecialidade(UUID novaEspecialidade) {
        if (novaEspecialidade == null) {
            throw new RegraDeNegocioException("A especialidade e obrigatoria.");
        }
        this.especialidadeId = novaEspecialidade;
        this.atualizadoEm = Instant.now();
    }

    /** RN-06: medico inativo nao pode ser escolhido em novo agendamento. */
    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Medico ja esta inativo.");
        }
        this.ativo = false;
        this.atualizadoEm = Instant.now();
    }

    public boolean pertenceAoUsuario(UUID candidato) {
        return usuarioId.equals(candidato);
    }

    private static String validarCrm(String valor) {
        String limpo = valor == null ? "" : valor.trim().replaceAll("\\s", "");
        if (limpo.length() < 4 || limpo.length() > 15) {
            throw new RegraDeNegocioException("O CRM deve ter entre 4 e 15 caracteres.");
        }
        return limpo;
    }

    private static String validarUf(String valor) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!UFS.contains(normalizado)) {
            throw new RegraDeNegocioException("UF do CRM invalida.");
        }
        return normalizado;
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
