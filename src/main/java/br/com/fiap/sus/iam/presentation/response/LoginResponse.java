package br.com.fiap.sus.iam.presentation.response;

import br.com.fiap.sus.iam.application.dto.AutenticacaoOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "LoginResponse", description = "Credencial de acesso emitida no login")
public record LoginResponse(
        @Schema(description = "Token JWT a ser enviado no header Authorization") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "Momento em que o token expira") Instant expiraEm,
        UsuarioResponse usuario) {

    public static LoginResponse de(AutenticacaoOutput output) {
        return new LoginResponse(output.token(), output.tipo(), output.expiraEm(),
                UsuarioResponse.de(output.usuario()));
    }
}
