package br.com.fiap.sus.pareceres.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de um parecer para leitura por outros modulos (historico). Sem a descricao clinica. */
public record ParecerResumo(
        UUID id,
        UUID resultadoExameId,
        UUID pacienteId,
        UUID medicoId,
        Instant dataParecer
) {
}
