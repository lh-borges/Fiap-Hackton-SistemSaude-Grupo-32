package br.com.fiap.sus.cadastros.presentation.controller;

import br.com.fiap.sus.cadastros.application.dto.TipoExameOutput;
import br.com.fiap.sus.cadastros.application.usecase.CadastrarTipoExameUseCase;
import br.com.fiap.sus.cadastros.application.usecase.InativarTipoExameUseCase;
import br.com.fiap.sus.cadastros.application.usecase.ListarTiposExameUseCase;
import br.com.fiap.sus.cadastros.presentation.request.CadastrarTipoExameRequest;
import br.com.fiap.sus.cadastros.presentation.response.TipoExameResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Tipos de exame")
@RestController
@RequestMapping("/api/v1/tipos-exame")
public class TipoExameController {

    private final CadastrarTipoExameUseCase cadastrar;
    private final ListarTiposExameUseCase listar;
    private final InativarTipoExameUseCase inativar;

    public TipoExameController(CadastrarTipoExameUseCase cadastrar, ListarTiposExameUseCase listar,
                               InativarTipoExameUseCase inativar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra um tipo de exame", description = "Exige ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<TipoExameResponse> cadastrar(
            @Valid @RequestBody CadastrarTipoExameRequest requisicao) {
        TipoExameOutput criado = cadastrar.executar(requisicao.nome(), requisicao.categoria(),
                requisicao.preparo());
        return ResponseEntity.created(URI.create("/api/v1/tipos-exame/" + criado.id()))
                .body(TipoExameResponse.de(criado));
    }

    @Operation(summary = "Lista tipos de exame",
            description = "A categoria define se o exame produz resultado de imagem ou laboratorial.")
    @GetMapping
    public PaginaResponse<TipoExameResponse> listar(
            @Parameter(description = "IMAGEM ou LABORATORIAL") @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        return PaginaResponse.of(listar.executar(categoria, ativo, pagina, tamanho), TipoExameResponse::de);
    }

    @Operation(summary = "Inativa um tipo de exame", description = "Exige ADMINISTRADOR.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
