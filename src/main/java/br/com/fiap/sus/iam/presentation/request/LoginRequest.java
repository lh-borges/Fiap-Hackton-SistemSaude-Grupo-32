package br.com.fiap.sus.iam.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credenciais de acesso")
public record LoginRequest(
        @Schema(example = "admin@sus.gov.br") @NotBlank(message = "informe o e-mail") String email,
        @Schema(example = "Admin@123") @NotBlank(message = "informe a senha") String senha) {
}
