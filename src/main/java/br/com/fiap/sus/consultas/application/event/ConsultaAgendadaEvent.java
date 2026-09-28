package br.com.fiap.sus.consultas.application.event;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Nova consulta criada. Notifica paciente e medico. */
@Externalized("sus.consultas.agendada.v1::#{#this.pacienteId()}")
public record ConsultaAgendadaEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID consultaId,
        UUID pacienteId,
        UUID medicoId,
        UUID unidadeSaudeId,
        Instant dataHora
) implements EventoDominio {

    public static ConsultaAgendadaEvent de(Consulta c) {
        return new ConsultaAgendadaEvent(UUID.randomUUID(), Instant.now(), c.getId(), c.getPacienteId(),
                c.getMedicoId(), c.getUnidadeSaudeId(), c.getDataHora());
    }
}
