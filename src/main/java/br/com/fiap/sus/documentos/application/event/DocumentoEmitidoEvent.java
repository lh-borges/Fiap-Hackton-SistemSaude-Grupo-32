package br.com.fiap.sus.documentos.application.event;

import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;

import java.time.Instant;
import java.util.UUID;

public record DocumentoEmitidoEvent(
        UUID documentoId,
        UUID pacienteId,
        UUID medicoId,
        TipoDocumento tipo,
        Instant dataEmissao
) {
}
