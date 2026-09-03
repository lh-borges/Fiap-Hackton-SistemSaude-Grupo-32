package br.com.fiap.sus.iam.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(name = "CadastrarUsuarioRequest")
public record CadastrarUsuarioRequest(
        @Schema(example = "Maria Souza")
        @NotBlank(message = "informe o nome") @Size(min = 3, max = 150) String nome,

        @Schema(example = "11144477735", description = "Somente digitos")
        @NotBlank(message = "informe o CPF") String cpf,

        @Schema(example = "maria.souza@sus.gov.br")
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail invalido") String email,

        @Schema(example = "Senha@123", description = "Minimo 8 caracteres, com letra e numero")
        @NotBlank(message = "informe a senha") String senha,

        @Schema(example = "[\"ATENDENTE\"]",
                description = "ADMINISTRADOR, ATENDENTE, MEDICO ou PACIENTE")
        @NotEmpty(message = "informe ao menos um perfil") Set<String> roles) {
}
