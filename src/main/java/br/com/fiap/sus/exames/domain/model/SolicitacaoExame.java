package br.com.fiap.sus.exames.domain.model;

import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/**
 * Pedido medico de exame (feature 004-exames). Separada da execucao (Exame) para manter
 * o pedido e a operacao da unidade como registros distintos e rastreaveis.
 */
public class SolicitacaoExame {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID tipoExameId;
    private final UUID consultaId;
    private final String justificativa;
    private SituacaoSolicitacaoExame situacao;
    private String motivoCancelamento;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private SolicitacaoExame(UUID id, UUID pacienteId, UUID medicoId, UUID tipoExameId, UUID consultaId,
                             String justificativa, SituacaoSolicitacaoExame situacao,
                             String motivoCancelamento, Instant criadoEm, Instant atualizadoEm) {
        if (pacienteId == null) {
            throw new RegraDeNegocioException("A solicitacao deve estar vinculada a um paciente.");
        }
        if (medicoId == null) {
            throw new RegraDeNegocioException("A solicitacao deve estar vinculada a um medico.");
        }
        if (tipoExameId == null) {
            throw new RegraDeNegocioException("A solicitacao deve informar o tipo de exame.");
        }
        if (justificativa == null || justificativa.trim().length() < 3) {
            throw new RegraDeNegocioException("A justificativa deve ter no minimo 3 caracteres.");
        }
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.tipoExameId = tipoExameId;
        this.consultaId = consultaId;
        this.justificativa = justificativa.trim();
        this.situacao = situacao;
        this.motivoCancelamento = motivoCancelamento;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** HU-01/RN-01: apenas medico cria; nasce PENDENTE. Vinculo com consulta e opcional. */
    public static SolicitacaoExame solicitar(UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                             UUID consultaId, String justificativa) {
        Instant agora = Instant.now();
        return new SolicitacaoExame(UUID.randomUUID(), pacienteId, medicoId, tipoExameId, consultaId,
                justificativa, SituacaoSolicitacaoExame.PENDENTE, null, agora, agora);
    }

    public static SolicitacaoExame reconstituir(UUID id, UUID pacienteId, UUID medicoId, UUID tipoExameId,
                                                UUID consultaId, String justificativa,
                                                SituacaoSolicitacaoExame situacao, String motivoCancelamento,
                                                Instant criadoEm, Instant atualizadoEm) {
        return new SolicitacaoExame(id, pacienteId, medicoId, tipoExameId, consultaId, justificativa,
                situacao, motivoCancelamento, criadoEm, atualizadoEm);
    }

    /** HU-02/EX-02: so agenda solicitacao PENDENTE (RN-02: no maximo um exame ativo). */
    public void marcarComoAgendada() {
        if (situacao != SituacaoSolicitacaoExame.PENDENTE) {
            throw new RegraDeNegocioException("Somente solicitacoes pendentes podem ser agendadas.");
        }
        this.situacao = SituacaoSolicitacaoExame.AGENDADA;
        this.atualizadoEm = Instant.now();
    }

    /** HU-03: espelha a realizacao do exame vinculado. */
    public void marcarComoRealizada() {
        if (situacao != SituacaoSolicitacaoExame.AGENDADA) {
            throw new RegraDeNegocioException("Somente solicitacoes agendadas podem ser marcadas como realizadas.");
        }
        this.situacao = SituacaoSolicitacaoExame.REALIZADA;
        this.atualizadoEm = Instant.now();
    }

    /** HU-04 (1o cenario)/RN-06: cancela a solicitacao em si; so enquanto PENDENTE. */
    public void cancelar(String motivo) {
        if (situacao != SituacaoSolicitacaoExame.PENDENTE) {
            throw new RegraDeNegocioException("Somente solicitacoes pendentes podem ser canceladas diretamente.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("O motivo do cancelamento e obrigatorio.");
        }
        this.situacao = SituacaoSolicitacaoExame.CANCELADA;
        this.motivoCancelamento = motivo.trim();
        this.atualizadoEm = Instant.now();
    }

    /**
     * HU-04 (2o cenario): quando o EXAME agendado (nao a solicitacao) e cancelado, a
     * solicitacao volta a PENDENTE para permitir novo agendamento. Chamado pelo usecase
     * que orquestra Exame.cancelar() + este metodo.
     */
    public void voltarParaPendente() {
        if (situacao != SituacaoSolicitacaoExame.AGENDADA) {
            throw new RegraDeNegocioException("Somente solicitacoes agendadas podem voltar a pendente.");
        }
        this.situacao = SituacaoSolicitacaoExame.PENDENTE;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public UUID getMedicoId() {
        return medicoId;
    }

    public UUID getTipoExameId() {
        return tipoExameId;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public SituacaoSolicitacaoExame getSituacao() {
        return situacao;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}