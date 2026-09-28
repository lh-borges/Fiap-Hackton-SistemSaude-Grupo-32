package br.com.fiap.sus.receitas.api;

import java.util.List;
import java.util.UUID;

/** Porta de leitura publicada pelo modulo receitas (Artigo III.3). */
public interface ReceitaQuery {

    /** Receitas do paciente, inclusive canceladas e renovadas, da mais recente para a mais antiga (feature 010). */
    List<ReceitaResumo> receitasDoPaciente(UUID pacienteId);
}
