package br.com.fiap.sus.consultas.application.event;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Data/hora da consulta alterada. Notifica paciente e medico. */
@Externalized("sus.consultas.remarcada.v1::#{#this.pacienteId()}")
public record ConsultaRemarcadaEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID consultaId,
        UUID pacienteId,
        UUID medicoId,
        Instant dataHoraAnterior,
        Instant dataHoraNova
) implements EventoDominio {

    public static ConsultaRemarcadaEvent de(Consulta c, Instant dataHoraAnterior) {
        return new ConsultaRemarcadaEvent(UUID.randomUUID(), Instant.now(), c.getId(), c.getPacienteId(),
                c.getMedicoId(), dataHoraAnterior, c.getDataHora());
    }
}
