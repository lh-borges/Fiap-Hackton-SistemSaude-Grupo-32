package br.com.fiap.sus.documentos.application.dto;

import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;

import java.time.Instant;
import java.util.UUID;

public record DocumentoMedicoOutput(
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
    public static DocumentoMedicoOutput deDetalhe(DocumentoMedico d) {
        return new DocumentoMedicoOutput(d.getId(), d.getTipo(), d.getConteudo(), d.getPacienteId(),
                d.getMedicoId(), d.getConsultaId(), d.getExameId(), d.getDataEmissao(), d.getArquivoUrl(), d.getSituacao(),
                d.getMotivoCancelamento());
    }

    public static DocumentoMedicoOutput deResumo(DocumentoMedico d) {
        return new DocumentoMedicoOutput(d.getId(), d.getTipo(), null, d.getPacienteId(), d.getMedicoId(),
                d.getConsultaId(), d.getExameId(), d.getDataEmissao(), d.getArquivoUrl(), d.getSituacao(),
                d.getMotivoCancelamento());
    }
}
