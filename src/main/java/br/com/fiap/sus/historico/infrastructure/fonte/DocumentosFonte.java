package br.com.fiap.sus.historico.infrastructure.fonte;

import br.com.fiap.sus.documentos.api.DocumentoQuery;
import br.com.fiap.sus.documentos.api.DocumentoResumo;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Documentos do paciente; aponta a consulta ou o exame de origem. Conteudo e arquivo nao sao expostos (RN-06). */
@Component
public class DocumentosFonte implements FonteHistorico {

    private final DocumentoQuery documentos;

    public DocumentosFonte(DocumentoQuery documentos) {
        this.documentos = documentos;
    }

    @Override
    public List<RegistroHistorico> registrosDoPaciente(UUID pacienteId) {
        return documentos.documentosDoPaciente(pacienteId).stream().map(DocumentosFonte::registro).toList();
    }

    static RegistroHistorico registro(DocumentoResumo d) {
        Referencia origem = d.consultaId() != null
                ? new Referencia(TipoRegistro.CONSULTA, d.consultaId())
                : Referencia.opcional(TipoRegistro.EXAME, d.exameId());
        return new RegistroHistorico(TipoRegistro.DOCUMENTO, d.id(), d.dataEmissao(),
                Titulos.comQualificador("Documento", d.tipo()), d.situacao(), d.medicoId(), origem);
    }
}
