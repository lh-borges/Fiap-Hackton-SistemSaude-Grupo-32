package br.com.fiap.sus.documentos.application.service;

import br.com.fiap.sus.documentos.api.DocumentoQuery;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentoQueryService implements DocumentoQuery {
    private final DocumentoMedicoRepository repository;

    public DocumentoQueryService(DocumentoMedicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existeDocumentoParaConsulta(UUID consultaId) {
        return repository.existePorConsultaId(consultaId);
    }
}
