package br.com.fiap.sus.documentos.api;

import java.util.List;
import java.util.UUID;

public interface DocumentoQuery {
    boolean existeDocumentoParaConsulta(UUID consultaId);

    /** Documentos do paciente, inclusive cancelados, do mais recente para o mais antigo (feature 010). */
    List<DocumentoResumo> documentosDoPaciente(UUID pacienteId);
}
