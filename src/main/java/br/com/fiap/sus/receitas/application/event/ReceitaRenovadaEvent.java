package br.com.fiap.sus.receitas.application.event;

import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Receita renovada a partir de outra. Notifica o paciente. */
@Externalized("sus.receitas.renovada.v1::#{#this.pacienteId()}")
public record ReceitaRenovadaEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID receitaId,
        UUID receitaOrigemId,
        UUID pacienteId,
        UUID medicoId
) implements EventoDominio {

    public static ReceitaRenovadaEvent de(Receita renovada, Receita origem) {
        return new ReceitaRenovadaEvent(UUID.randomUUID(), Instant.now(), renovada.getId(), origem.getId(),
                renovada.getPacienteId(), renovada.getMedicoId());
    }
}
