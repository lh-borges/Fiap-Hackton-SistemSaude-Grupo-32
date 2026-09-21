package br.com.fiap.sus.pareceres.api;

import java.util.UUID;

public interface ParecerQuery {

    boolean existeParecerParaResultado(UUID resultadoExameId);
}
