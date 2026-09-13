package br.com.fiap.sus.exames.domain.model;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/**
 * Execucao/agendamento de uma solicitacao de exame em uma unidade de saude
 * (feature 004-exames). RN-02: pertence a exatamente uma solicitacao.
 */
public class Exame {

    private final UUID id;
    private final UUID solicitacaoExameId;
    private final UUID unidadeSaudeId;
    private Instant dataAgendada;
    private Instant dataRealizacao;
    private SituacaoExame situacao;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Exame(UUID id, UUID solicitacaoExameId, UUID unidadeSaudeId, Instant dataAgendada,
                  Instant dataRealizacao, SituacaoExame situacao, Instant criadoEm, Instant atualizadoEm) {
        if (solicitacaoExameId == null) {
            throw new RegraDeNegocioException("O exame deve estar vinculado a uma solicitacao.");
        }
        if (unidadeSaudeId == null) {
            throw new RegraDeNegocioException("O exame deve estar vinculado a uma unidade de saude.");
        }
        this.id = id;
        this.solicitacaoExameId = solicitacaoExameId;
        this.unidadeSaudeId = unidadeSaudeId;
        this.dataAgendada = dataAgendada;
        this.dataRealizacao = dataRealizacao;
        this.situacao = situacao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** HU-02/RN-05: data de agendamento e sempre futura. */
    public static Exame agendar(UUID solicitacaoExameId, UUID unidadeSaudeId, Instant dataAgendada) {
        if (dataAgendada == null || !dataAgendada.isAfter(Instant.now())) {
            throw new RegraDeNegocioException("A data de agendamento do exame deve ser futura.");
        }
        Instant agora = Instant.now();
        return new Exame(UUID.randomUUID(), solicitacaoExameId, unidadeSaudeId, dataAgendada, null,
                SituacaoExame.AGENDADO, agora, agora);
    }

    public static Exame reconstituir(UUID id, UUID solicitacaoExameId, UUID unidadeSaudeId,
                                     Instant dataAgendada, Instant dataRealizacao, SituacaoExame situacao,
                                     Instant criadoEm, Instant atualizadoEm) {
        return new Exame(id, solicitacaoExameId, unidadeSaudeId, dataAgendada, dataRealizacao, situacao,
                criadoEm, atualizadoEm);
    }

    /** HU-03/RN-05/EX-03: so registra realizacao de exame AGENDADO, com data nao futura. */
    public void registrarRealizacao(Instant dataRealizacao) {
        if (situacao != SituacaoExame.AGENDADO) {
            throw new RegraDeNegocioException("Somente exames agendados podem ter a realizacao registrada.");
        }
        if (dataRealizacao == null || dataRealizacao.isAfter(Instant.now())) {
            throw new RegraDeNegocioException("A data de realizacao nao pode ser futura.");
        }
        this.dataRealizacao = dataRealizacao;
        this.situacao = SituacaoExame.REALIZADO;
        this.atualizadoEm = Instant.now();
    }

    /** RN-06/EX-04: exame realizado nao pode ser cancelado. */
    public void cancelar() {
        if (situacao == SituacaoExame.REALIZADO) {
            throw new RegraDeNegocioException("Exame ja realizado nao pode ser cancelado.");
        }
        if (situacao == SituacaoExame.CANCELADO) {
            throw new RegraDeNegocioException("Exame ja esta cancelado.");
        }
        this.situacao = SituacaoExame.CANCELADO;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitacaoExameId() {
        return solicitacaoExameId;
    }

    public UUID getUnidadeSaudeId() {
        return unidadeSaudeId;
    }

    public Instant getDataAgendada() {
        return dataAgendada;
    }

    public Instant getDataRealizacao() {
        return dataRealizacao;
    }

    public SituacaoExame getSituacao() {
        return situacao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}