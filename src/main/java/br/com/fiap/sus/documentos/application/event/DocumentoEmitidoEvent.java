package br.com.fiap.sus.documentos.application.event;

import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.Externalized;

import java.time.Instant;
import java.util.UUID;

/**
 * Novo documento medico disponivel. Notifica o paciente.
 * {@code tipo} e o nome do tipo (ATESTADO, LAUDO...), como texto, para que o contrato
 * nao dependa do enum interno do modulo.
 */
@Externalized("sus.documentos.emitido.v1::#{#this.pacienteId()}")
public record DocumentoEmitidoEvent(
        UUID eventoId,
        Instant ocorridoEm,
        UUID documentoId,
        UUID pacienteId,
        UUID medicoId,
        String tipo,
        Instant dataEmissao
) implements EventoDominio {

    public static DocumentoEmitidoEvent de(DocumentoMedico d) {
        return new DocumentoEmitidoEvent(UUID.randomUUID(), Instant.now(), d.getId(), d.getPacienteId(),
                d.getMedicoId(), d.getTipo().name(), d.getDataEmissao());
    }
}
