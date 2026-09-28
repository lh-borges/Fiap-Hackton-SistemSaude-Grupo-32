package br.com.fiap.sus.documentos.api;

import java.time.Instant;
import java.util.UUID;

/** Projecao leve de um documento medico para leitura por outros modulos (historico). Sem conteudo nem arquivo. */
public record DocumentoResumo(
        UUID id,
        String tipo,
        UUID pacienteId,
        UUID medicoId,
        UUID consultaId,
        UUID exameId,
        Instant dataEmissao,
        String situacao
) {
}
