package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(name = "CadastrarMedicoRequest")
public record CadastrarMedicoRequest(
        @Schema(description = "Usuario com perfil MEDICO ao qual o cadastro sera vinculado")
        @NotNull(message = "informe o usuario") UUID usuarioId,

        @Schema(example = "123456")
        @NotBlank(message = "informe o CRM") String crm,

        @Schema(example = "SP")
        @NotBlank(message = "informe a UF do CRM") @Size(min = 2, max = 2) String ufCrm,

        @NotNull(message = "informe a especialidade") UUID especialidadeId) {
}
