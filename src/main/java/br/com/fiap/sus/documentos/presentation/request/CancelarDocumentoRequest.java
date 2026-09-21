package br.com.fiap.sus.documentos.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarDocumentoRequest(
        @NotBlank(message = "informe o motivo do cancelamento") @Size(max = 1000) String motivo
) {
}
