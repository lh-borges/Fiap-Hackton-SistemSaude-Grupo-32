package br.com.fiap.sus.cadastros.presentation.controller;

import br.com.fiap.sus.cadastros.application.dto.AtualizarPacienteInput;
import br.com.fiap.sus.cadastros.application.dto.CadastrarPacienteInput;
import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.application.usecase.AtualizarPacienteUseCase;
import br.com.fiap.sus.cadastros.application.usecase.BuscarMeuCadastroDePacienteUseCase;
import br.com.fiap.sus.cadastros.application.usecase.BuscarPacienteUseCase;
import br.com.fiap.sus.cadastros.application.usecase.CadastrarPacienteUseCase;
import br.com.fiap.sus.cadastros.application.usecase.InativarPacienteUseCase;
import br.com.fiap.sus.cadastros.application.usecase.ListarPacientesUseCase;
import br.com.fiap.sus.cadastros.presentation.request.AtualizarPacienteRequest;
import br.com.fiap.sus.cadastros.presentation.request.CadastrarPacienteRequest;
import br.com.fiap.sus.cadastros.presentation.response.PacienteResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pacientes")
@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    private final CadastrarPacienteUseCase cadastrar;
    private final AtualizarPacienteUseCase atualizar;
    private final BuscarPacienteUseCase buscar;
    private final BuscarMeuCadastroDePacienteUseCase meuCadastro;
    private final ListarPacientesUseCase listar;
    private final InativarPacienteUseCase inativar;

    public PacienteController(CadastrarPacienteUseCase cadastrar, AtualizarPacienteUseCase atualizar,
                              BuscarPacienteUseCase buscar, BuscarMeuCadastroDePacienteUseCase meuCadastro,
                              ListarPacientesUseCase listar, InativarPacienteUseCase inativar) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.buscar = buscar;
        this.meuCadastro = meuCadastro;
        this.listar = listar;
        this.inativar = inativar;
    }

    @Operation(summary = "Cadastra um paciente", description = "Exige ADMINISTRADOR ou ATENDENTE.")
    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody CadastrarPacienteRequest requisicao) {
        PacienteOutput criado = cadastrar.executar(new CadastrarPacienteInput(requisicao.usuarioId(),
                requisicao.cartaoSus(), requisicao.dataNascimento(), requisicao.sexo(),
                requisicao.tipoSanguineo()));
        return ResponseEntity.created(URI.create("/api/v1/pacientes/" + criado.id()))
                .body(PacienteResponse.de(criado));
    }

    @Operation(summary = "Lista pacientes",
            description = "Busca por nome, CPF ou cartao SUS. Exige ADMINISTRADOR, ATENDENTE ou MEDICO.")
    @GetMapping
    public PaginaResponse<PacienteResponse> listar(
            @Parameter(description = "Nome, CPF ou cartao SUS") @RequestParam(required = false) String termo,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.of(listar.executar(termo, ativo, pagina, tamanho), PacienteResponse::de);
    }

    @Operation(summary = "Cadastro do paciente autenticado",
            description = "Como o paciente descobre o proprio pacienteId para as demais rotas.")
    @GetMapping("/me")
    public PacienteResponse meuCadastro() {
        return PacienteResponse.de(meuCadastro.executar());
    }

    @Operation(summary = "Consulta um paciente",
            description = "O paciente so acessa o proprio cadastro; cadastro de terceiro responde 404.")
    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable UUID id) {
        return PacienteResponse.de(buscar.executar(id));
    }

    @Operation(summary = "Atualiza dados do paciente", description = "Exige ADMINISTRADOR ou ATENDENTE.")
    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable UUID id,
                                      @Valid @RequestBody AtualizarPacienteRequest requisicao) {
        return PacienteResponse.de(atualizar.executar(id, new AtualizarPacienteInput(
                requisicao.dataNascimento(), requisicao.sexo(), requisicao.tipoSanguineo())));
    }

    @Operation(summary = "Inativa um paciente", description = "Exclusao logica: o historico e preservado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        inativar.executar(id);
        return ResponseEntity.noContent().build();
    }
}
