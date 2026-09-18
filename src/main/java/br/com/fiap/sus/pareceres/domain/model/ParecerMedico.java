package br.com.fiap.sus.pareceres.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

public final class ParecerMedico {
    private static final int DESCRICAO_MIN = 10;
    private static final int DESCRICAO_MAX = 5000;

    private final UUID id;
    private final UUID resultadoExameId;
    private final UUID medicoId;
    private final UUID pacienteId;
    private final String descricao;
    private final Instant dataParecer;

    private ParecerMedico(UUID id, UUID resultadoExameId, UUID pacienteId, UUID medicoId, String descricao,
                          Instant dataParecer) {
        if (isNull(id)) {
            throw new RegraDeNegocioException("O identificador do parecer e obrigatorio.");
        }
        if (isNull(dataParecer)) {
            throw new RegraDeNegocioException("A data do parecer e obrigatoria.");
        }
        if (isNull(resultadoExameId)) {
            throw new RegraDeNegocioException("O parecer deve estar vinculado a um resultado de exame.");
        }
        if (isNull(pacienteId)) {
            throw new RegraDeNegocioException("O parecer deve estar vinculado a um paciente.");
        }
        if (isNull(medicoId)) {
            throw new RegraDeNegocioException("O parecer deve ter um medico responsavel.");
        }

        String descricaoTratada = descricao == null ? "" : descricao.trim();
        if (descricaoTratada.length() < DESCRICAO_MIN || descricaoTratada.length() > DESCRICAO_MAX) {
            throw new RegraDeNegocioException(
                    "A descricao do parecer deve ter entre " + DESCRICAO_MIN + " e " + DESCRICAO_MAX
                            + " caracteres.");
        }
        this.id = id;
        this.resultadoExameId = resultadoExameId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.descricao = descricaoTratada;
        this.dataParecer = dataParecer;
    }

    public static ParecerMedico emitir(
            UUID resultadoExameId,
            UUID pacienteId,
            UUID medicoId,
            String descricao
    ) {
        return new ParecerMedico(UUID.randomUUID(), resultadoExameId, pacienteId, medicoId, descricao, Instant.now());
    }

    public static ParecerMedico reconstituir(
            UUID id,
            UUID resultadoExameId,
            UUID pacienteId,
            UUID medicoId,
            String descricao,
            Instant dataParecer
    ) {
        return new ParecerMedico(id, resultadoExameId, pacienteId, medicoId, descricao, dataParecer);
    }

    public UUID getId() { return id; }
    public UUID getResultadoExameId() { return resultadoExameId; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public String getDescricao() { return descricao; }
    public Instant getDataParecer() { return dataParecer; }
}
