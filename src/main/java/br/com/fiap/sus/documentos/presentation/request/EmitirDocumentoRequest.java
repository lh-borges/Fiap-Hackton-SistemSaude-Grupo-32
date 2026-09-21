package br.com.fiap.sus.documentos.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EmitirDocumentoRequest(
        @NotNull(message = "informe o paciente")
        UUID pacienteId,
        @NotBlank(message = "informe o tipo do documento")
        String tipo,
        @NotBlank(message = "informe o conteudo") @Size(min = 10, max = 10000)
        String conteudo,
        UUID consultaId,
        UUID exameId
) {
}
