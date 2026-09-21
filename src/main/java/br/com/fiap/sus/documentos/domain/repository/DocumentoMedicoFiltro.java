package br.com.fiap.sus.documentos.domain.repository;

import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record DocumentoMedicoFiltro(
        UUID pacienteId,
        UUID medicoId,
        TipoDocumento tipo,
        Instant periodoInicio,
        Instant periodoFim,
        Set<UUID> pacientesPermitidos
) {
    public DocumentoMedicoFiltro {
        pacientesPermitidos = pacientesPermitidos == null ? null : Set.copyOf(pacientesPermitidos);
    }

    public DocumentoMedicoFiltro(UUID pacienteId, UUID medicoId, TipoDocumento tipo,
                                 Instant periodoInicio, Instant periodoFim) {
        this(pacienteId, medicoId, tipo, periodoInicio, periodoFim, null);
    }
}
