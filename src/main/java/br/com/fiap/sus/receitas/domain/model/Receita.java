package br.com.fiap.sus.receitas.domain.model;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Prescricao emitida por um medico para um paciente (feature 007-receitas). */
public class Receita {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID consultaId;
    private final List<ItemReceita> itens;
    private final Instant dataEmissao;
    private final Instant validade;
    private String observacao;
    private SituacaoReceita situacao;
    private final UUID receitaOrigemId;
    private String motivoCancelamento;

    private Receita(UUID id, UUID pacienteId, UUID medicoId, UUID consultaId, List<ItemReceita> itens,
                    Instant dataEmissao, Instant validade, String observacao, SituacaoReceita situacao,
                    UUID receitaOrigemId, String motivoCancelamento) {
        if (pacienteId == null) {
            throw new RegraDeNegocioException("A receita deve estar vinculada a um paciente.");
        }
        if (medicoId == null) {
            throw new RegraDeNegocioException("A receita deve estar vinculada a um medico.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("A receita deve conter pelo menos um item.");
        }
        if (validade == null || dataEmissao == null || !validade.isAfter(dataEmissao)) {
            throw new RegraDeNegocioException("A validade deve ser posterior a data de emissao.");
        }
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.consultaId = consultaId;
        this.itens = new ArrayList<>(itens);
        this.dataEmissao = dataEmissao;
        this.validade = validade;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.situacao = situacao;
        this.receitaOrigemId = receitaOrigemId;
        this.motivoCancelamento = motivoCancelamento;
    }

    public static Receita emitir(UUID pacienteId, UUID medicoId, UUID consultaId, List<ItemReceita> itens,
                                 Instant validade, String observacao) {
        Instant agora = Instant.now();
        return new Receita(UUID.randomUUID(), pacienteId, medicoId, consultaId, itens, agora, validade,
                observacao, SituacaoReceita.ATIVA, null, null);
    }

    public static Receita reconstituir(UUID id, UUID pacienteId, UUID medicoId, UUID consultaId,
                                       List<ItemReceita> itens, Instant dataEmissao, Instant validade,
                                       String observacao, SituacaoReceita situacao, UUID receitaOrigemId,
                                       String motivoCancelamento) {
        return new Receita(id, pacienteId, medicoId, consultaId, itens, dataEmissao, validade, observacao,
                situacao, receitaOrigemId, motivoCancelamento);
    }

    public Receita renovar(UUID novaConsultaId, Instant novaValidade, String novaObservacao) {
        if (situacao != SituacaoReceita.ATIVA) {
            throw new RegraDeNegocioException("Somente receitas ativas podem ser renovadas.");
        }
        if (estaVencida()) {
            throw new RegraDeNegocioException("Receita vencida nao pode ser renovada; emita uma nova.");
        }
        Instant agora = Instant.now();
        Receita nova = new Receita(UUID.randomUUID(), pacienteId, medicoId, novaConsultaId,
                new ArrayList<>(itens), agora, novaValidade, novaObservacao, SituacaoReceita.ATIVA,
                this.id, null);
        this.situacao = SituacaoReceita.RENOVADA;
        return nova;
    }

    public void cancelar(UUID medicoSolicitante, String motivo) {
        if (!medicoId.equals(medicoSolicitante)) {
            throw new RegraDeNegocioException("Somente o medico autor pode cancelar a receita.");
        }
        if (situacao == SituacaoReceita.CANCELADA) {
            throw new RegraDeNegocioException("Receita ja esta cancelada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("O motivo do cancelamento e obrigatorio.");
        }
        this.situacao = SituacaoReceita.CANCELADA;
        this.motivoCancelamento = motivo.trim();
    }

    public boolean estaVencida() {
        return situacao == SituacaoReceita.ATIVA && validade.isBefore(Instant.now());
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

    public UUID getConsultaId() {
        return consultaId;
    }

    public List<ItemReceita> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public Instant getDataEmissao() {
        return dataEmissao;
    }

    public Instant getValidade() {
        return validade;
    }

    public String getObservacao() {
        return observacao;
    }

    public SituacaoReceita getSituacao() {
        return situacao;
    }

    public UUID getReceitaOrigemId() {
        return receitaOrigemId;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }
}