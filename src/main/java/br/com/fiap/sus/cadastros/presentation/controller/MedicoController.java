package br.com.fiap.sus.cadastros.presentation.controller;

import br.com.fiap.sus.cadastros.application.dto.CadastrarMedicoInput;
import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.application.usecase.AtualizarMedicoUseCase;
import br.com.fiap.sus.cadastros.application.usecase.BuscarMedicoUseCase;
import br.com.fiap.sus.cadastros.application.usecase.BuscarMeuCadastroDeMedicoUseCase;
import br.com.fiap.sus.cadastros.application.usecase.CadastrarMedicoUseCase;
import br.com.fiap.sus.cadastros.application.usecase.InativarMedicoUseCase;
import br.com.fiap.sus.cadastros.application.usecase.ListarMedicosUseCase;
import br.com.fiap.sus.cadastros.presentation.request.AtualizarMedicoRequest;
import br.com.fiap.sus.cadastros.presentation.request.CadastrarMedicoRequest;
import br.com.fiap.sus.cadastros.presentation.response.MedicoResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Medicos")
@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final CadastrarMedicoUseCase cadastrar;
    private final AtualizarMedicoUseCase atualizar;
    private final BuscarMedicoUseCase buscar;
    private final BuscarMeuCadastroDeMedicoUseCase meuCadastro;
    private final ListarMedicosUseCase listar;
    private final InativarMedicoUseCase inativar;

    public MedicoController(CadastrarMedicoUseCase cadastrar, AtualizarMedicoUseCase atualizar,
                            BuscarMedicoUseCase buscar, BuscarMeuCadastroDeMedicoUseCase meuCadastro,
                            ListarMedicosUseCase listar, InativarMedicoUseCase inativar) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.buscar = buscar;
        this.meuCadastro = meuCadastro;
        this.listar = listar;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra um medico", description = "Exige ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<MedicoResponse> cadastrar(@Valid @RequestBody CadastrarMedicoRequest requisicao) {
        MedicoOutput criado = cadastrar.executar(new CadastrarMedicoInput(requisicao.usuarioId(),
                requisicao.crm(), requisicao.ufCrm(), requisicao.especialidadeId()));
        return ResponseEntity.created(URI.create("/api/v1/medicos/" + criado.id()))
                .body(MedicoResponse.de(criado));
    }

    @Operation(summary = "Lista medicos", description = "Pode filtrar por especialidade.")
    @GetMapping
    public PaginaResponse<MedicoResponse> listar(
            @RequestParam(required = false) UUID especialidadeId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.of(listar.executar(especialidadeId, ativo, pagina, tamanho), MedicoResponse::de);
    }

    @Operation(summary = "Cadastro do medico autenticado",
            description = "Como o medico descobre o proprio medicoId para as demais rotas.")
    @GetMapping("/me")
    public MedicoResponse meuCadastro() {
        return MedicoResponse.de(meuCadastro.executar());
    }

    @Operation(summary = "Consulta um medico")
    @GetMapping("/{id}")
    public MedicoResponse buscar(@PathVariable UUID id) {
        return MedicoResponse.de(buscar.executar(id));
    }

    @Operation(summary = "Altera a especialidade do medico", description = "Exige ADMINISTRADOR.")
    @PutMapping("/{id}")
    public MedicoResponse atualizar(@PathVariable UUID id,
                                    @Valid @RequestBody AtualizarMedicoRequest requisicao) {
        return MedicoResponse.de(atualizar.executar(id, requisicao.especialidadeId()));
    }

    @Operation(summary = "Inativa um medico",
            description = "Exclusao logica: pareceres e receitas emitidos continuam validos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
