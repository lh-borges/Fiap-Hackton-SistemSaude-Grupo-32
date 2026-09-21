package br.com.fiap.sus.documentos.infrastructure.persistence.mapper;

import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.infrastructure.persistence.entity.DocumentoMedicoEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentoPersistenceMapper {
    public DocumentoMedicoEntity paraEntidade(DocumentoMedico documento) {
        return new DocumentoMedicoEntity(documento.getId(), documento.getTipo(), documento.getConteudo(),
                documento.getPacienteId(), documento.getMedicoId(), documento.getConsultaId(),
                documento.getExameId(), documento.getDataEmissao(), documento.getArquivoUrl(), documento.getSituacao(),
                documento.getMotivoCancelamento());
    }

    public DocumentoMedico paraDominio(DocumentoMedicoEntity entity) {
        return DocumentoMedico.reconstituir(entity.getId(), entity.getTipo(), entity.getConteudo(),
                entity.getPacienteId(), entity.getMedicoId(), entity.getConsultaId(), entity.getExameId(), entity.getDataEmissao(),
                entity.getArquivoUrl(), entity.getSituacao(), entity.getMotivoCancelamento());
    }
}
