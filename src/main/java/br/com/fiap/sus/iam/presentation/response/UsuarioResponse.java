package br.com.fiap.sus.iam.presentation.response;

import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(name = "Usuario", description = "Usuario do sistema. A senha nunca e devolvida.")
public record UsuarioResponse(
        UUID id,
        @Schema(example = "Maria Souza") String nome,
        @Schema(example = "111.444.777-35") String cpf,
        @Schema(example = "maria.souza@sus.gov.br") String email,
        boolean ativo,
        @Schema(example = "[\"ATENDENTE\"]") Set<String> roles,
        Instant criadoEm) {

    public static UsuarioResponse de(UsuarioOutput output) {
        return new UsuarioResponse(output.id(), output.nome(), output.cpf(), output.email(),
                output.ativo(), output.roles(), output.criadoEm());
    }
}
