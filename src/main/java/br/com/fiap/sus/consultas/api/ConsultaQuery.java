package br.com.fiap.sus.consultas.api;

import java.util.Set;
import java.util.UUID;

/** Vinculo assistencial estabelecido por consultas nao canceladas. */
public interface ConsultaQuery {
    Set<UUID> pacientesDoMedico(UUID medicoId);
}
