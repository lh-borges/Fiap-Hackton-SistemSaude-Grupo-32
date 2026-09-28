package br.com.fiap.sus.receitas.application.event;

import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Nova receita disponibilizada. Notifica o paciente. Os itens nao viajam no evento. */
@Externalized("sus.receitas.emitida.v1::#{#this.pacienteId()}")
public record ReceitaEmitidaEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID receitaId,
        UUID pacienteId,
        UUID medicoId,
        Instant validade,
        int quantidadeItens
) implements EventoDominio {

    public static ReceitaEmitidaEvent de(Receita r) {
        return new ReceitaEmitidaEvent(UUID.randomUUID(), Instant.now(), r.getId(), r.getPacienteId(),
                r.getMedicoId(), r.getValidade(), r.getItens().size());
    }
}
