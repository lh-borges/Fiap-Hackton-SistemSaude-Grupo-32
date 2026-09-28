package br.com.fiap.sus.pareceres.application.event;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Medico concluiu a analise de um resultado. Notifica o paciente. A descricao nao viaja no evento. */
@Externalized("sus.pareceres.criado.v1::#{#this.pacienteId()}")
public record ParecerCriadoEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID parecerId,
        UUID resultadoExameId,
        UUID pacienteId,
        UUID medicoId,
        Instant dataParecer
) implements EventoDominio {

    public static ParecerCriadoEvent de(ParecerMedico p) {
        return new ParecerCriadoEvent(UUID.randomUUID(), Instant.now(), p.getId(), p.getResultadoExameId(),
                p.getPacienteId(), p.getMedicoId(), p.getDataParecer());
    }
}
