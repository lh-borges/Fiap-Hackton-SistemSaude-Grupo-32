package br.com.fiap.sus.cadastros.presentation.controller;

import br.com.fiap.sus.cadastros.application.dto.UnidadeSaudeOutput;
import br.com.fiap.sus.cadastros.application.usecase.CadastrarUnidadeSaudeUseCase;
import br.com.fiap.sus.cadastros.application.usecase.InativarUnidadeSaudeUseCase;
import br.com.fiap.sus.cadastros.application.usecase.ListarUnidadesSaudeUseCase;
import br.com.fiap.sus.cadastros.presentation.request.CadastrarUnidadeSaudeRequest;
import br.com.fiap.sus.cadastros.presentation.response.UnidadeSaudeResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Unidades de saude")
@RestController
@RequestMapping("/api/v1/unidades-saude")
public class UnidadeSaudeController {

    private final CadastrarUnidadeSaudeUseCase cadastrar;
    private final ListarUnidadesSaudeUseCase listar;
    private final InativarUnidadeSaudeUseCase inativar;

    public UnidadeSaudeController(CadastrarUnidadeSaudeUseCase cadastrar, ListarUnidadesSaudeUseCase listar,
                                  InativarUnidadeSaudeUseCase inativar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra uma unidade de saude", description = "Exige ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<UnidadeSaudeResponse> cadastrar(
            @Valid @RequestBody CadastrarUnidadeSaudeRequest requisicao) {
        UnidadeSaudeOutput criada = cadastrar.executar(requisicao.nome(), requisicao.cnes(),
                requisicao.telefone());
        return ResponseEntity.created(URI.create("/api/v1/unidades-saude/" + criada.id()))
                .body(UnidadeSaudeResponse.de(criada));
    }

    @Operation(summary = "Lista unidades de saude")
    @GetMapping
    public PaginaResponse<UnidadeSaudeResponse> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        return PaginaResponse.of(listar.executar(ativo, pagina, tamanho), UnidadeSaudeResponse::de);
    }

    @Operation(summary = "Inativa uma unidade de saude", description = "Exige ADMINISTRADOR.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
