package br.com.fiap.sus.consultas.domain.model;

import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.UUID;

/**
 * Encontro agendado entre paciente e medico em uma unidade de saude (feature 003-consultas).
 * Ponto de partida do restante do atendimento: solicitacao de exame, receita e documento
 * nascem de uma consulta.
 */
public class Consulta {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID unidadeSaudeId;
    private Instant dataHora;
    private SituacaoConsulta situacao;
    private final String motivo;
    private String observacoes;
    private String motivoCancelamento;
    private boolean remarcada;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Consulta(UUID id, UUID pacienteId, UUID medicoId, UUID unidadeSaudeId, Instant dataHora,
                     SituacaoConsulta situacao, String motivo, String observacoes,
                     String motivoCancelamento, boolean remarcada, Instant criadoEm, Instant atualizadoEm) {
        if (pacienteId == null) {
            throw new RegraDeNegocioException("A consulta deve estar vinculada a um paciente.");
        }
        if (medicoId == null) {
            throw new RegraDeNegocioException("A consulta deve estar vinculada a um medico.");
        }
        if (unidadeSaudeId == null) {
            throw new RegraDeNegocioException("A consulta deve estar vinculada a uma unidade de saude.");
        }
        if (motivo == null || motivo.trim().length() < 3) {
            throw new RegraDeNegocioException("O motivo da consulta deve ter no minimo 3 caracteres.");
        }
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.unidadeSaudeId = unidadeSaudeId;
        this.dataHora = dataHora;
        this.situacao = situacao;
        this.motivo = motivo.trim();
        this.observacoes = observacoes;
        this.motivoCancelamento = motivoCancelamento;
        this.remarcada = remarcada;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** HU-01/RN-01: so agenda para data e hora futuras. */
    public static Consulta agendar(UUID pacienteId, UUID medicoId, UUID unidadeSaudeId, Instant dataHora,
                                   String motivo) {
        if (dataHora == null || !dataHora.isAfter(Instant.now())) {
            throw new RegraDeNegocioException("A consulta so pode ser agendada para uma data e hora futuras.");
        }
        Instant agora = Instant.now();
        return new Consulta(UUID.randomUUID(), pacienteId, medicoId, unidadeSaudeId, dataHora,
                SituacaoConsulta.AGENDADA, motivo, null, null, false, agora, agora);
    }

    public static Consulta reconstituir(UUID id, UUID pacienteId, UUID medicoId, UUID unidadeSaudeId,
                                        Instant dataHora, SituacaoConsulta situacao, String motivo,
                                        String observacoes, String motivoCancelamento, boolean remarcada,
                                        Instant criadoEm, Instant atualizadoEm) {
        return new Consulta(id, pacienteId, medicoId, unidadeSaudeId, dataHora, situacao, motivo,
                observacoes, motivoCancelamento, remarcada, criadoEm, atualizadoEm);
    }

    /** HU-02/EX-03: so remarca consulta ainda AGENDADA, para data futura. */
    public void remarcar(Instant novaDataHora) {
        if (situacao != SituacaoConsulta.AGENDADA) {
            throw new RegraDeNegocioException("Somente consultas agendadas podem ser remarcadas.");
        }
        if (novaDataHora == null || !novaDataHora.isAfter(Instant.now())) {
            throw new RegraDeNegocioException("A nova data e hora devem ser futuras.");
        }
        this.dataHora = novaDataHora;
        this.remarcada = true;
        this.atualizadoEm = Instant.now();
    }

    /** HU-03/RN-06: cancelamento exige motivo; nao cancela consulta ja finalizada. */
    public void cancelar(String motivoCancelamento) {
        if (situacao == SituacaoConsulta.REALIZADA || situacao == SituacaoConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Consulta realizada ou ja cancelada nao pode ser cancelada.");
        }
        if (motivoCancelamento == null || motivoCancelamento.isBlank()) {
            throw new RegraDeNegocioException("O motivo do cancelamento e obrigatorio.");
        }
        this.situacao = SituacaoConsulta.CANCELADA;
        this.motivoCancelamento = motivoCancelamento.trim();
        this.atualizadoEm = Instant.now();
    }

    /**
     * HU-04/RN-05/EX-05: so o medico da consulta registra a realizacao, e a data da
     * consulta ja deve ter ocorrido.
     */
    public void registrarRealizacao(UUID medicoSolicitante, String observacoesAtendimento) {
        if (!medicoId.equals(medicoSolicitante)) {
            throw new RegraDeNegocioException("Somente o medico da consulta pode registrar a realizacao.");
        }
        if (situacao == SituacaoConsulta.REALIZADA || situacao == SituacaoConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Consulta ja esta realizada ou cancelada.");
        }
        if (dataHora.isAfter(Instant.now())) {
            throw new RegraDeNegocioException("Nao e possivel registrar realizacao de consulta futura.");
        }
        this.situacao = SituacaoConsulta.REALIZADA;
        this.observacoes = observacoesAtendimento;
        this.atualizadoEm = Instant.now();
    }

    /**
     * RN-04: consulta realizada ou cancelada e imutavel, exceto pelas observacoes do
     * medico autor, que pode complementa-las mesmo depois de finalizada.
     */
    public void alterarObservacoes(UUID medicoSolicitante, String novasObservacoes) {
        if (!medicoId.equals(medicoSolicitante)) {
            throw new RegraDeNegocioException("Somente o medico autor pode alterar as observacoes.");
        }
        this.observacoes = novasObservacoes;
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

    public UUID getUnidadeSaudeId() {
        return unidadeSaudeId;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public SituacaoConsulta getSituacao() {
        return situacao;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public boolean isRemarcada() {
        return remarcada;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}