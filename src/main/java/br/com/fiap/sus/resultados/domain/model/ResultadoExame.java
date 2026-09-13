package br.com.fiap.sus.resultados.domain.model;

import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Dado tecnico produzido pela realizacao de um exame (feature 005-resultados).
 * RN-03: no maximo um resultado por exame (garantido pelo usecase/repository, unique em exameId).
 * RN-08: imutavel apos criado.
 */
public class ResultadoExame {

    private final UUID id;
    private final UUID exameId;
    private final UUID pacienteId;
    private final TipoResultado tipoResultado;
    private final Instant dataResultado;
    private final String observacao;

    // preenchidos somente quando tipoResultado == IMAGEM
    private final String arquivoUrl;
    private final String descricao;
    private final String laudo;

    // preenchidos somente quando tipoResultado == LABORATORIAL
    private final List<ItemResultadoLaboratorial> itens;

    private ResultadoExame(UUID id, UUID exameId, UUID pacienteId, TipoResultado tipoResultado,
                           Instant dataResultado, String observacao, String arquivoUrl, String descricao,
                           String laudo, List<ItemResultadoLaboratorial> itens) {
        if (exameId == null) {
            throw new RegraDeNegocioException("O resultado deve estar vinculado a um exame.");
        }
        if (pacienteId == null) {
            throw new RegraDeNegocioException("O resultado deve estar vinculado a um paciente.");
        }
        if (tipoResultado == null) {
            throw new RegraDeNegocioException("O tipo do resultado e obrigatorio.");
        }
        this.id = id;
        this.exameId = exameId;
        this.pacienteId = pacienteId;
        this.tipoResultado = tipoResultado;
        this.dataResultado = dataResultado;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.arquivoUrl = arquivoUrl;
        this.descricao = descricao;
        this.laudo = laudo;
        this.itens = itens == null ? Collections.emptyList() : List.copyOf(itens);
    }

    /** HU-01: resultado de imagem. */
    public static ResultadoExame criarImagem(UUID exameId, UUID pacienteId, String arquivoUrl,
                                             String descricao, String laudo, String observacao) {
        if (arquivoUrl == null || arquivoUrl.isBlank()) {
            throw new RegraDeNegocioException("A referencia do arquivo e obrigatoria para resultado de imagem.");
        }
        return new ResultadoExame(UUID.randomUUID(), exameId, pacienteId, TipoResultado.IMAGEM,
                Instant.now(), observacao, arquivoUrl.trim(), descricao, laudo, null);
    }

    /** HU-02/RN-05: resultado laboratorial com ao menos um parametro. */
    public static ResultadoExame criarLaboratorial(UUID exameId, UUID pacienteId,
                                                   List<ItemResultadoLaboratorial> itens, String observacao) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("O resultado laboratorial deve conter ao menos um parametro.");
        }
        return new ResultadoExame(UUID.randomUUID(), exameId, pacienteId, TipoResultado.LABORATORIAL,
                Instant.now(), observacao, null, null, null, itens);
    }

    public static ResultadoExame reconstituir(UUID id, UUID exameId, UUID pacienteId,
                                              TipoResultado tipoResultado, Instant dataResultado,
                                              String observacao, String arquivoUrl, String descricao,
                                              String laudo, List<ItemResultadoLaboratorial> itens) {
        return new ResultadoExame(id, exameId, pacienteId, tipoResultado, dataResultado, observacao,
                arquivoUrl, descricao, laudo, itens);
    }

    public UUID getId() {
        return id;
    }

    public UUID getExameId() {
        return exameId;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public TipoResultado getTipoResultado() {
        return tipoResultado;
    }

    public Instant getDataResultado() {
        return dataResultado;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getArquivoUrl() {
        return arquivoUrl;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLaudo() {
        return laudo;
    }

    public List<ItemResultadoLaboratorial> getItens() {
        return itens;
    }
}