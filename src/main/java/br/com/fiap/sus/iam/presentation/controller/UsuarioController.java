package br.com.fiap.sus.iam.presentation.controller;

import br.com.fiap.sus.iam.application.dto.AlterarSenhaInput;
import br.com.fiap.sus.iam.application.dto.AtualizarUsuarioInput;
import br.com.fiap.sus.iam.application.dto.CadastrarUsuarioInput;
import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.application.usecase.AlterarSenhaUseCase;
import br.com.fiap.sus.iam.application.usecase.AtribuirRolesUseCase;
import br.com.fiap.sus.iam.application.usecase.AtualizarUsuarioUseCase;
import br.com.fiap.sus.iam.application.usecase.BuscarUsuarioUseCase;
import br.com.fiap.sus.iam.application.usecase.CadastrarUsuarioUseCase;
import br.com.fiap.sus.iam.application.usecase.InativarUsuarioUseCase;
import br.com.fiap.sus.iam.application.usecase.ListarUsuariosUseCase;
import br.com.fiap.sus.iam.presentation.request.AlterarSenhaRequest;
import br.com.fiap.sus.iam.presentation.request.AtribuirRolesRequest;
import br.com.fiap.sus.iam.presentation.request.AtualizarUsuarioRequest;
import br.com.fiap.sus.iam.presentation.request.CadastrarUsuarioRequest;
import br.com.fiap.sus.iam.presentation.response.UsuarioResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrar;
    private final ListarUsuariosUseCase listar;
    private final BuscarUsuarioUseCase buscar;
    private final AtualizarUsuarioUseCase atualizar;
    private final AlterarSenhaUseCase alterarSenha;
    private final AtribuirRolesUseCase atribuirRoles;
    private final InativarUsuarioUseCase inativar;

    public UsuarioController(CadastrarUsuarioUseCase cadastrar, ListarUsuariosUseCase listar,
                             BuscarUsuarioUseCase buscar, AtualizarUsuarioUseCase atualizar,
                             AlterarSenhaUseCase alterarSenha, AtribuirRolesUseCase atribuirRoles,
                             InativarUsuarioUseCase inativar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.buscar = buscar;
        this.atualizar = atualizar;
        this.alterarSenha = alterarSenha;
        this.atribuirRoles = atribuirRoles;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra um usuario", description = "Exige perfil ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissao", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail ja cadastrado", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "422", description = "Regra de negocio violada", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastrarUsuarioRequest requisicao) {
        UsuarioOutput criado = cadastrar.executar(new CadastrarUsuarioInput(
                requisicao.nome(), requisicao.cpf(), requisicao.email(), requisicao.senha(), requisicao.roles()));
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + criado.id()))
                .body(UsuarioResponse.de(criado));
    }

    @Operation(summary = "Lista usuarios", description = "Exige perfil ADMINISTRADOR. Resultado paginado.")
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @Parameter(description = "Busca por nome, e-mail ou CPF") @RequestParam(required = false) String termo,
            @Parameter(description = "ADMINISTRADOR, ATENDENTE, MEDICO ou PACIENTE") @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.of(listar.executar(termo, role, ativo, pagina, tamanho), UsuarioResponse::de);
    }

    @Operation(summary = "Consulta um usuario",
            description = "O administrador consulta qualquer usuario; os demais perfis, apenas a si mesmos.")
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id) {
        return UsuarioResponse.de(buscar.executar(id));
    }

    @Operation(summary = "Atualiza nome e e-mail", description = "Administrador ou o proprio usuario. O CPF nao muda.")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id,
                                     @Valid @RequestBody AtualizarUsuarioRequest requisicao) {
        return UsuarioResponse.de(atualizar.executar(id,
                new AtualizarUsuarioInput(requisicao.nome(), requisicao.email())));
    }

    @Operation(summary = "Altera a senha",
            description = "O proprio usuario deve informar a senha atual; o administrador pode redefinir sem ela.")
    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> alterarSenha(@PathVariable UUID id,
                                             @Valid @RequestBody AlterarSenhaRequest requisicao) {
        alterarSenha.executar(id, new AlterarSenhaInput(requisicao.senhaAtual(), requisicao.novaSenha()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Substitui os perfis do usuario", description = "Exige perfil ADMINISTRADOR.")
    @PutMapping("/{id}/roles")
    public UsuarioResponse atribuirRoles(@PathVariable UUID id,
                                         @Valid @RequestBody AtribuirRolesRequest requisicao) {
        return UsuarioResponse.de(atribuirRoles.executar(id, requisicao.roles()));
    }

    @Operation(summary = "Inativa um usuario",
            description = "Exclusao logica (Artigo V.6): o registro e preservado para o historico.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario inativado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "422", description = "Usuario ja inativo ou tentativa de auto-inativacao", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
