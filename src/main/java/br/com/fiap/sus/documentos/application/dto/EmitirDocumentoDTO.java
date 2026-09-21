package br.com.fiap.sus.documentos.application.dto;

import java.util.UUID;

public record EmitirDocumentoDTO(
        UUID pacienteId,
        String tipo,
        String conteudo,
        UUID consultaId,
        UUID exameId
) {
}
