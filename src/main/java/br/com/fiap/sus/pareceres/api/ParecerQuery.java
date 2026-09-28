package br.com.fiap.sus.pareceres.api;

import java.util.List;
import java.util.UUID;

public interface ParecerQuery {

    boolean existeParecerParaResultado(UUID resultadoExameId);

    /** Pareceres do paciente, do mais recente para o mais antigo (feature 010). */
    List<ParecerResumo> pareceresDoPaciente(UUID pacienteId);
}
