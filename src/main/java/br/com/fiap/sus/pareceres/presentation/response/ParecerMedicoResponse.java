package br.com.fiap.sus.pareceres.presentation.response;

import br.com.fiap.sus.pareceres.application.dto.ParecerMedicoOutput;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "ParecerMedicoResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParecerMedicoResponse(
        UUID id,
        UUID resultadoExameId,
        UUID pacienteId,
        UUID medicoId,
        UUID idEspecialidade,
        String medicoCrm,
        String medicoNome,
        @Schema(description = "Descricao clinica; omitida na consulta administrativa") String descricao,
        Instant dataParecer) {

    public static ParecerMedicoResponse de(ParecerMedicoOutput output) {
        return new ParecerMedicoResponse(output.id(), output.resultadoExameId(), output.pacienteId(),
                output.medicoId(), output.idEspecialidade(), output.medicoCrm(), output.medicoNome(),
                output.descricao(), output.dataParecer());
    }
}
