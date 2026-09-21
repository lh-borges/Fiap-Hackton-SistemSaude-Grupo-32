package br.com.fiap.sus.resultados.api;

import java.util.Set;
import java.util.UUID;

/** Extensao de leitura implementada por pareceres, sem dependencia circular. */
public interface IndicadorParecerQuery {
    Set<UUID> resultadosComParecer(Set<UUID> resultadoExameIds);
}
