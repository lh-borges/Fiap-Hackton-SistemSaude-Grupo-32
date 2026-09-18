package br.com.fiap.sus.pareceres.domain.repository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ParecerMedicoFiltro(UUID pacienteId, UUID medicoId, UUID resultadoExameId,
                                 Instant periodoInicio, Instant periodoFim, Set<UUID> pacientesPermitidos) {
    public ParecerMedicoFiltro {
        pacientesPermitidos = pacientesPermitidos == null ? null : Set.copyOf(pacientesPermitidos);
    }

    public ParecerMedicoFiltro(UUID pacienteId, UUID medicoId, UUID resultadoExameId,
                              Instant periodoInicio, Instant periodoFim) {
        this(pacienteId, medicoId, resultadoExameId, periodoInicio, periodoFim, null);
    }
}
