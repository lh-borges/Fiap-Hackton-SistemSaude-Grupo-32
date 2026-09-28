package br.com.fiap.sus.documentos.domain.repository;

import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.shared.domain.PaginaResultado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoMedicoRepository {

    DocumentoMedico salvar(DocumentoMedico documento);

    Optional<DocumentoMedico> buscarPorId(UUID id);

    boolean existePorConsultaId(UUID consultaId);

    PaginaResultado<DocumentoMedico> listar(DocumentoMedicoFiltro filtro, int pagina, int tamanho);

    /** Feature 010: todos os documentos do paciente, do mais recente para o mais antigo. */
    List<DocumentoMedico> listarPorPaciente(UUID pacienteId);
}
