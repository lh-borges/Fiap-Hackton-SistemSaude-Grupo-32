package br.com.fiap.sus.documentos.application.service;

import br.com.fiap.sus.documentos.api.DocumentoQuery;
import br.com.fiap.sus.documentos.api.DocumentoResumo;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import java.util.List;
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

    @Override
    public List<DocumentoResumo> documentosDoPaciente(UUID pacienteId) {
        return repository.listarPorPaciente(pacienteId).stream().map(DocumentoQueryService::resumo).toList();
    }

    private static DocumentoResumo resumo(DocumentoMedico d) {
        return new DocumentoResumo(d.getId(), d.getTipo().name(), d.getPacienteId(), d.getMedicoId(),
                d.getConsultaId(), d.getExameId(), d.getDataEmissao(), d.getSituacao().name());
    }
}
