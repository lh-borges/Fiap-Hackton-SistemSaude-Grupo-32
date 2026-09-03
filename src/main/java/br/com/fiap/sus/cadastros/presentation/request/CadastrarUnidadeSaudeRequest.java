package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CadastrarUnidadeSaudeRequest")
public record CadastrarUnidadeSaudeRequest(
        @Schema(example = "UBS Jardim Sao Paulo")
        @NotBlank(message = "informe o nome") @Size(min = 3, max = 150) String nome,
        @Schema(example = "9876543", description = "7 digitos")
        @NotBlank(message = "informe o CNES") String cnes,
        @Schema(example = "1130003000") String telefone) {
}
