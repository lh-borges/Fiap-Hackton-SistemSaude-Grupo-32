package br.com.fiap.sus.pareceres.application.event;

import java.time.Instant;
import java.util.UUID;

public record ParecerCriadoEvent(
        UUID parecerId,
        UUID resultadoExameId,
        UUID pacienteId,
        UUID medicoId,
        Instant dataParecer
) {
}
