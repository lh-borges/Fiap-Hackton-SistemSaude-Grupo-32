package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "CadastrarPacienteRequest")
public record CadastrarPacienteRequest(
        @Schema(description = "Usuario com perfil PACIENTE ao qual o cadastro sera vinculado")
        @NotNull(message = "informe o usuario") UUID usuarioId,

        @Schema(example = "123456789012345", description = "15 digitos")
        @NotBlank(message = "informe o cartao SUS") String cartaoSus,

        @Schema(example = "1985-04-12")
        @NotNull(message = "informe a data de nascimento")
        @Past(message = "a data de nascimento deve estar no passado") LocalDate dataNascimento,

        @Schema(example = "FEMININO", description = "MASCULINO, FEMININO, OUTRO ou NAO_INFORMADO")
        @NotBlank(message = "informe o sexo") String sexo,

        @Schema(example = "O+", description = "Opcional") String tipoSanguineo) {
}
