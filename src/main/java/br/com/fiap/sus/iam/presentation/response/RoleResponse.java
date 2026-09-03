package br.com.fiap.sus.iam.presentation.response;

import br.com.fiap.sus.iam.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "Role", description = "Perfil de autorizacao")
public record RoleResponse(UUID id, @Schema(example = "MEDICO") String nome) {

    public static RoleResponse de(Role role) {
        return new RoleResponse(role.id(), role.nome().name());
    }
}
