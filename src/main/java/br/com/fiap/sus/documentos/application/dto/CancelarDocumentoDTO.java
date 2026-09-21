package br.com.fiap.sus.documentos.application.dto;

import java.util.UUID;

public record CancelarDocumentoDTO(
        UUID documentoId,
        String motivo
) {
}
