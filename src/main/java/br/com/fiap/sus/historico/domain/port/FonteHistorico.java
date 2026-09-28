package br.com.fiap.sus.historico.domain.port;

import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import java.util.List;
import java.util.UUID;

/** Porta de saida: cada modulo clinico e uma fonte que devolve seus registros para um paciente (RF-01). */
public interface FonteHistorico {
    List<RegistroHistorico> registrosDoPaciente(UUID pacienteId);
}
