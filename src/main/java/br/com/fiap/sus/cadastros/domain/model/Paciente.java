package br.com.fiap.sus.cadastros.domain.model;

import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Dados de dominio do paciente. O vinculo com o usuario e por identificador (Artigo III.4). */
public class Paciente {

    private static final java.util.Set<String> TIPOS_SANGUINEOS =
            java.util.Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private final UUID id;
    private final UUID usuarioId;
    private final String cartaoSus;
    private LocalDate dataNascimento;
    private Sexo sexo;
    private String tipoSanguineo;
    private boolean ativo;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Paciente(UUID id, UUID usuarioId, String cartaoSus, LocalDate dataNascimento, Sexo sexo,
                     String tipoSanguineo, boolean ativo, Instant criadoEm, Instant atualizadoEm) {
        if (usuarioId == null) {
            throw new RegraDeNegocioException("O paciente deve estar vinculado a um usuario.");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.cartaoSus = validarCartaoSus(cartaoSus);
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.ativo = ativo;
        alterarDados(dataNascimento, sexo, tipoSanguineo);
    }

    public static Paciente criar(UUID usuarioId, String cartaoSus, LocalDate dataNascimento, Sexo sexo,
                                 String tipoSanguineo) {
        Instant agora = Instant.now();
        return new Paciente(UUID.randomUUID(), usuarioId, cartaoSus, dataNascimento, sexo, tipoSanguineo,
                true, agora, agora);
    }

    public static Paciente reconstituir(UUID id, UUID usuarioId, String cartaoSus, LocalDate dataNascimento,
                                        Sexo sexo, String tipoSanguineo, boolean ativo,
                                        Instant criadoEm, Instant atualizadoEm) {
        return new Paciente(id, usuarioId, cartaoSus, dataNascimento, sexo, tipoSanguineo, ativo,
                criadoEm, atualizadoEm);
    }

    public final void alterarDados(LocalDate novaDataNascimento, Sexo novoSexo, String novoTipoSanguineo) {
        // RN-05: data de nascimento nao pode ser futura.
        if (novaDataNascimento == null || novaDataNascimento.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("A data de nascimento deve ser valida e nao pode ser futura.");
        }
        if (novoSexo == null) {
            throw new RegraDeNegocioException("O sexo e obrigatorio.");
        }
        this.dataNascimento = novaDataNascimento;
        this.sexo = novoSexo;
        this.tipoSanguineo = validarTipoSanguineo(novoTipoSanguineo);
        this.atualizadoEm = Instant.now();
    }

    /** RN-06: paciente inativo nao pode ser referenciado em novo agendamento. */
    public void inativar() {
        if (!ativo) {
            throw new RegraDeNegocioException("Paciente ja esta inativo.");
        }
        this.ativo = false;
        this.atualizadoEm = Instant.now();
    }

    public boolean pertenceAoUsuario(UUID candidato) {
        return usuarioId.equals(candidato);
    }

    private static String validarCartaoSus(String valor) {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        if (digitos.length() != 15) {
            throw new RegraDeNegocioException("O cartao SUS deve ter 15 digitos.");
        }
        return digitos;
    }

    private static String validarTipoSanguineo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String normalizado = valor.trim().toUpperCase();
        if (!TIPOS_SANGUINEOS.contains(normalizado)) {
            throw new RegraDeNegocioException("Tipo sanguineo invalido. Ex.: A+, O-, AB+.");
        }
        return normalizado;
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
