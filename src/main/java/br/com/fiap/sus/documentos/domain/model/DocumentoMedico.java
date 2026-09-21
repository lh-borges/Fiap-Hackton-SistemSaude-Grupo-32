package br.com.fiap.sus.documentos.domain.model;

import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

public class DocumentoMedico {

    private static final int CONTEUDO_MIN = 10;
    private static final int CONTEUDO_MAX = 10000;

    private final UUID id;
    private final TipoDocumento tipo;
    private final String conteudo;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID consultaId;
    private final UUID exameId;
    private final Instant dataEmissao;
    private final String arquivoUrl;
    private final SituacaoDocumento situacao;
    private final String motivoCancelamento;

    private DocumentoMedico(
            UUID id,
            TipoDocumento tipo,
            String conteudo,
            UUID pacienteId,
            UUID medicoId,
            UUID consultaId,
            UUID exameId,
            Instant dataEmissao,
            String arquivoUrl,
            SituacaoDocumento situacao,
            String motivoCancelamento
    ) {
        if (isNull(tipo)) {
            throw new RegraDeNegocioException("O tipo do documento é obrigatório.");
        }
        if (isNull(pacienteId)) {
            throw new RegraDeNegocioException("O documento deve estar vinculado a um paciente.");
        }
        if (isNull(medicoId)) {
            throw new RegraDeNegocioException("O documento deve ter um medico responsavel.");
        }

        String conteudoTratado = conteudo == null ? "" : conteudo.trim();
        if (conteudoTratado.length() < CONTEUDO_MIN || conteudoTratado.length() > CONTEUDO_MAX) {
            throw new RegraDeNegocioException(
                    "O conteudo do documento deve ter entre " + CONTEUDO_MIN + " e " + CONTEUDO_MAX
                            + " caracteres.");
        }
        if (situacao == SituacaoDocumento.CANCELADO
                && (isNull(motivoCancelamento) || motivoCancelamento.isBlank())) {
            throw new RegraDeNegocioException("O cancelamento do documento exige um motivo.");
        }
        this.id = id;
        this.tipo = tipo;
        this.conteudo = conteudoTratado;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.consultaId = consultaId;
        this.exameId = exameId;
        this.dataEmissao = dataEmissao;
        this.arquivoUrl = arquivoUrl;
        this.situacao = situacao;
        this.motivoCancelamento = motivoCancelamento;
    }

    public static DocumentoMedico emitir(
            TipoDocumento tipo,
            String conteudo,
            UUID pacienteId,
            UUID medicoId,

            UUID consultaId
    ) {
        return emitir(tipo, conteudo, pacienteId, medicoId, consultaId, null);
    }

    public static DocumentoMedico emitir(
            TipoDocumento tipo,
            String conteudo,
            UUID pacienteId,
            UUID medicoId,
            UUID consultaId,
            UUID exameId
    ) {
        return new DocumentoMedico(UUID.randomUUID(), tipo, conteudo, pacienteId, medicoId, consultaId,
                exameId, Instant.now(), null, SituacaoDocumento.EMITIDO, null);
    }

    public static DocumentoMedico reconstituir(
            UUID id,
            TipoDocumento tipo,
            String conteudo,
            UUID pacienteId,
            UUID medicoId,
            UUID consultaId,
            UUID exameId,
            Instant dataEmissao,
            String arquivoUrl,
            SituacaoDocumento situacao,
            String motivoCancelamento
    ) {
        return new DocumentoMedico(id, tipo, conteudo, pacienteId, medicoId, consultaId, exameId, dataEmissao, arquivoUrl,
                situacao, motivoCancelamento);
    }

    public DocumentoMedico cancelar(String motivo) {
        if (this.situacao == SituacaoDocumento.CANCELADO) {
            throw new RegraDeNegocioException("O documento já está cancelado.");
        }
        return new DocumentoMedico(this.id, this.tipo, this.conteudo, this.pacienteId, this.medicoId,
                this.consultaId, this.exameId, this.dataEmissao, this.arquivoUrl, SituacaoDocumento.CANCELADO, motivo);
    }

    public UUID getId() { return id; }
    public TipoDocumento getTipo() { return tipo; }
    public String getConteudo() { return conteudo; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getConsultaId() { return consultaId; }
    public UUID getExameId() { return exameId; }
    public Instant getDataEmissao() { return dataEmissao; }
    public String getArquivoUrl() { return arquivoUrl; }
    public SituacaoDocumento getSituacao() { return situacao; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
}
