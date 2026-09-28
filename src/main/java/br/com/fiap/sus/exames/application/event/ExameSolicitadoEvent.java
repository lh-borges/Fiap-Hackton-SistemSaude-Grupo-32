package br.com.fiap.sus.exames.application.event;

import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/** Medico solicitou um exame. Notifica o paciente. A justificativa nao viaja no evento. */
@Externalized("sus.exames.solicitado.v1::#{#this.pacienteId()}")
public record ExameSolicitadoEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID solicitacaoId,
        UUID pacienteId,
        UUID medicoId,
        UUID tipoExameId
) implements EventoDominio {

    public static ExameSolicitadoEvent de(SolicitacaoExame s) {
        return new ExameSolicitadoEvent(UUID.randomUUID(), Instant.now(), s.getId(), s.getPacienteId(),
                s.getMedicoId(), s.getTipoExameId());
    }
}
