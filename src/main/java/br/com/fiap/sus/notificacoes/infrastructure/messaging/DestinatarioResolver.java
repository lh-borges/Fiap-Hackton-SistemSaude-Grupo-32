package br.com.fiap.sus.notificacoes.infrastructure.messaging;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.notificacoes.application.dto.Destinatario;
import br.com.fiap.sus.notificacoes.application.dto.PapelDestinatario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converte os identificadores clinicos do evento (pacienteId, medicoId) no usuario
 * destinatario, pela porta publicada de cadastros (Artigo III.3). Quando o vinculo nao
 * existe, devolve um destinatario nao resolvido para que o processador o descarte (RN-10).
 */
@Component
public class DestinatarioResolver {

    private final CadastroQuery cadastros;

    public DestinatarioResolver(CadastroQuery cadastros) {
        this.cadastros = cadastros;
    }

    public Destinatario paciente(UUID pacienteId) {
        UUID usuarioId = pacienteId == null ? null
                : cadastros.resumoDoPaciente(pacienteId).map(PacienteResumo::usuarioId).orElse(null);
        return new Destinatario(PapelDestinatario.PACIENTE, usuarioId);
    }

    public Destinatario medico(UUID medicoId) {
        UUID usuarioId = medicoId == null ? null
                : cadastros.resumoDoMedico(medicoId).map(MedicoResumo::usuarioId).orElse(null);
        return new Destinatario(PapelDestinatario.MEDICO, usuarioId);
    }
}
