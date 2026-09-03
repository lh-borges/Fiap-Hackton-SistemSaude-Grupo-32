package br.com.fiap.sus.cadastros.presentation.controller;

import br.com.fiap.sus.cadastros.application.dto.EspecialidadeOutput;
import br.com.fiap.sus.cadastros.application.usecase.CadastrarEspecialidadeUseCase;
import br.com.fiap.sus.cadastros.application.usecase.InativarEspecialidadeUseCase;
import br.com.fiap.sus.cadastros.application.usecase.ListarEspecialidadesUseCase;
import br.com.fiap.sus.cadastros.presentation.request.CadastrarEspecialidadeRequest;
import br.com.fiap.sus.cadastros.presentation.response.EspecialidadeResponse;
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

@Tag(name = "Especialidades")
@RestController
@RequestMapping("/api/v1/especialidades")
public class EspecialidadeController {

    private final CadastrarEspecialidadeUseCase cadastrar;
    private final ListarEspecialidadesUseCase listar;
    private final InativarEspecialidadeUseCase inativar;

    public EspecialidadeController(CadastrarEspecialidadeUseCase cadastrar,
                                   ListarEspecialidadesUseCase listar,
                                   InativarEspecialidadeUseCase inativar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra uma especialidade", description = "Exige ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<EspecialidadeResponse> cadastrar(
            @Valid @RequestBody CadastrarEspecialidadeRequest requisicao) {
        EspecialidadeOutput criada = cadastrar.executar(requisicao.nome(), requisicao.descricao());
        return ResponseEntity.created(URI.create("/api/v1/especialidades/" + criada.id()))
                .body(EspecialidadeResponse.de(criada));
    }

    @Operation(summary = "Lista especialidades")
    @GetMapping
    public PaginaResponse<EspecialidadeResponse> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        return PaginaResponse.de(listar.executar(ativo, pagina, tamanho), EspecialidadeResponse::de);
    }

    @Operation(summary = "Inativa uma especialidade", description = "Exige ADMINISTRADOR.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
