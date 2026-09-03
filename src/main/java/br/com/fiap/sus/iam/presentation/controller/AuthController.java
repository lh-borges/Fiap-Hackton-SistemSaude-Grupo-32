package br.com.fiap.sus.iam.presentation.controller;

import br.com.fiap.sus.iam.application.usecase.AutenticarUsuarioUseCase;
import br.com.fiap.sus.iam.application.usecase.BuscarUsuarioLogadoUseCase;
import br.com.fiap.sus.iam.presentation.request.LoginRequest;
import br.com.fiap.sus.iam.presentation.response.LoginResponse;
import br.com.fiap.sus.iam.presentation.response.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticacao")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticar;
    private final BuscarUsuarioLogadoUseCase usuarioLogado;

    public AuthController(AutenticarUsuarioUseCase autenticar, BuscarUsuarioLogadoUseCase usuarioLogado) {
        this.autenticar = autenticar;
        this.usuarioLogado = usuarioLogado;
    }

    @Operation(summary = "Autentica e emite o token de acesso",
            description = "Rota publica. Use o token devolvido no botao Authorize do Swagger.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas ou usuario inativo", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest requisicao) {
        return LoginResponse.de(autenticar.executar(requisicao.email(), requisicao.senha()));
    }

    @Operation(summary = "Dados do usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuario da sessao"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> eu() {
        return ResponseEntity.ok(UsuarioResponse.de(usuarioLogado.executar()));
    }
}
