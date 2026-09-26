package br.com.fiap.sus.exames.application.event;

import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Exame agendado em uma unidade. Notifica o paciente. */
@Externalized("sus.exames.agendado.v1::#{#this.pacienteId()}")
public record ExameAgendadoEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID exameId,
        UUID solicitacaoId,
        UUID pacienteId,
        UUID unidadeSaudeId,
        Instant dataAgendada
) implements EventoDominio {

    public static ExameAgendadoEvent de(Exame e, SolicitacaoExame s) {
        return new ExameAgendadoEvent(UUID.randomUUID(), Instant.now(), e.getId(), s.getId(), s.getPacienteId(),
                e.getUnidadeSaudeId(), e.getDataAgendada());
    }
}
