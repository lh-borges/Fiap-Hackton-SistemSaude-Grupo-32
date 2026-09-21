package br.com.fiap.sus.documentos.api;

import java.util.UUID;

public interface DocumentoQuery {
    boolean existeDocumentoParaConsulta(UUID consultaId);
}
