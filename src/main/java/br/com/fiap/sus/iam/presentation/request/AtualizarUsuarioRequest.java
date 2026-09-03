package br.com.fiap.sus.iam.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AtualizarUsuarioRequest")
public record AtualizarUsuarioRequest(
        @NotBlank(message = "informe o nome") @Size(min = 3, max = 150) String nome,
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail invalido") String email) {
}
