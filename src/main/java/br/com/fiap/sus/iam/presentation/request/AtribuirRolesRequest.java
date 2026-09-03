package br.com.fiap.sus.iam.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

@Schema(name = "AtribuirRolesRequest")
public record AtribuirRolesRequest(
        @Schema(example = "[\"MEDICO\"]")
        @NotEmpty(message = "informe ao menos um perfil") Set<String> roles) {
}
