package br.com.fiap.sus.pareceres.application.dto;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;

import java.time.Instant;
import java.util.UUID;

public record ParecerMedicoOutput(
        UUID id,
        UUID resultadoExameId,
        UUID pacienteId,
        UUID medicoId,
        UUID idEspecialidade,
        String medicoCrm,
        String medicoNome,
        String descricao,
        Instant dataParecer
) {

    public static ParecerMedicoOutput of(ParecerMedico parecer, UUID idEspecialidade, String medicoCrm, String medicoNome) {
        return new ParecerMedicoOutput(parecer.getId(), parecer.getResultadoExameId(), parecer.getPacienteId(),
                parecer.getMedicoId(), idEspecialidade, medicoCrm, medicoNome, parecer.getDescricao(), parecer.getDataParecer());
    }
}
