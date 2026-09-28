package br.com.fiap.sus.consultas.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Vinculo assistencial estabelecido por consultas nao canceladas. */
public interface ConsultaQuery {
    Set<UUID> pacientesDoMedico(UUID medicoId);

    Optional<UUID> pacienteIdDaConsulta(UUID consultaId);

    Optional<Instant> dataHoraDaConsulta(UUID consultaId);

    /** Todas as consultas do paciente, inclusive canceladas, da mais recente para a mais antiga (feature 010). */
    List<ConsultaResumo> consultasDoPaciente(UUID pacienteId);
}
