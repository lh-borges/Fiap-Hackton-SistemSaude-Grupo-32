package br.com.fiap.sus.consultas.api;

import java.util.Optional;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Vinculo assistencial estabelecido por consultas nao canceladas. */
public interface ConsultaQuery {
    Set<UUID> pacientesDoMedico(UUID medicoId);

    Optional<UUID> pacienteIdDaConsulta(UUID consultaId);

    Optional<Instant> dataHoraDaConsulta(UUID consultaId);
}
