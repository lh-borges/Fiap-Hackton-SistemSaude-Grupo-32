package br.com.fiap.sus.consultas.application.event;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Consulta cancelada. Notifica paciente e medico. O motivo nao viaja no evento. */
@Externalized("sus.consultas.cancelada.v1::#{#this.pacienteId()}")
public record ConsultaCanceladaEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID consultaId,
        UUID pacienteId,
        UUID medicoId,
        Instant dataHora,
        UUID canceladaPorUsuarioId
) implements EventoDominio {

    public static ConsultaCanceladaEvent de(Consulta c, UUID canceladaPorUsuarioId) {
        return new ConsultaCanceladaEvent(UUID.randomUUID(), Instant.now(), c.getId(), c.getPacienteId(),
                c.getMedicoId(), c.getDataHora(), canceladaPorUsuarioId);
    }
}
