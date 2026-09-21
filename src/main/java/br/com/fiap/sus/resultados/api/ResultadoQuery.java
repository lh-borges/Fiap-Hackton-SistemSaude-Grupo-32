package br.com.fiap.sus.resultados.api;

import java.util.Optional;
import java.util.UUID;

/** Leitura de referencias do resultado, sem expor ou modificar seu conteudo clinico. */
public interface ResultadoQuery {

    Optional<UUID> pacienteIdDoResultado(UUID resultadoId);

    Optional<ResultadoResumo> resultadoDoExame(UUID exameId);
}
