package br.com.fiap.sus.resultados.application.event;

import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado tecnico registrado. Notifica paciente e medico solicitante.
 * {@code medicoSolicitanteId} pode ser nulo quando a solicitacao de origem nao for localizada.
 */
@Externalized("sus.resultados.disponivel.v1::#{#this.pacienteId()}")
public record ResultadoExameDisponivelEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID resultadoId,
        UUID exameId,
        UUID pacienteId,
        UUID medicoSolicitanteId,
        String tipoResultado
) implements EventoDominio {

    public static ResultadoExameDisponivelEvent de(ResultadoExame r, UUID medicoSolicitanteId) {
        return new ResultadoExameDisponivelEvent(UUID.randomUUID(), Instant.now(), r.getId(), r.getExameId(),
                r.getPacienteId(), medicoSolicitanteId, r.getTipoResultado().name());
    }
}
