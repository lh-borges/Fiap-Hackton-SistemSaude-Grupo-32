package br.com.fiap.sus.resultados.api;

import java.time.Instant;
import java.util.UUID;

public record ResultadoResumo(
        UUID id,
        UUID exameId,
        UUID pacienteId,
        String tipoResultado,
        Instant dataResultado,
        String observacao,
        String arquivoUrl,
        String descricao,
        String laudo
) {
}
