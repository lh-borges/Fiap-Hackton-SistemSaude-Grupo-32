package br.com.fiap.sus.pareceres.presentation.controller;

import br.com.fiap.sus.pareceres.application.dto.RegistrarParecerDTO;
import br.com.fiap.sus.pareceres.application.usecase.ConsultarParecerUseCase;
import br.com.fiap.sus.pareceres.application.usecase.ListarPareceresUseCase;
import br.com.fiap.sus.pareceres.application.usecase.RegistrarParecerUseCase;
import br.com.fiap.sus.pareceres.application.usecase.RejeitarAlteracaoParecerUseCase;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.presentation.request.RegistrarParecerRequest;
import br.com.fiap.sus.pareceres.presentation.response.ParecerMedicoResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pareceres medicos")
@RestController
@RequestMapping("/api/v1/pareceres")
public class ParecerMedicoController {
    private final RegistrarParecerUseCase registrar;
    private final ConsultarParecerUseCase consultar;
    private final ListarPareceresUseCase listar;
    private final RejeitarAlteracaoParecerUseCase rejeitarAlteracao;

    public ParecerMedicoController(RegistrarParecerUseCase registrar, ConsultarParecerUseCase consultar,
                                   ListarPareceresUseCase listar, RejeitarAlteracaoParecerUseCase rejeitarAlteracao) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.listar = listar;
        this.rejeitarAlteracao = rejeitarAlteracao;
    }

    @Operation(summary = "Registra parecer medico", description = "Exige MEDICO ativo. Paciente, autor e data sao definidos pelo servidor.")
    @PostMapping
    public ResponseEntity<ParecerMedicoResponse> registrar(@Valid @RequestBody RegistrarParecerRequest request) {
        var criado = registrar.executar(new RegistrarParecerDTO(request.resultadoExameId(), request.descricao()));
        return ResponseEntity.created(URI.create("/api/v1/pareceres/" + criado.id()))
                .body(ParecerMedicoResponse.de(criado));
    }

    @Operation(summary = "Consulta parecer", description = "PACIENTE acessa os proprios; MEDICO acessa pacientes atendidos; ADMINISTRADOR recebe metadados.")
    @GetMapping("/{id}")
    public ParecerMedicoResponse consultar(@PathVariable UUID id) {
        return ParecerMedicoResponse.de(consultar.executar(id));
    }

    @Operation(summary = "Lista pareceres com filtros", description = "A restricao de acesso e aplicada antes da paginacao. ATENDENTE nao tem acesso.")
    @GetMapping
    public PaginaResponse<ParecerMedicoResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) UUID resultadoExameId,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var filtro = new ParecerMedicoFiltro(pacienteId, medicoId, resultadoExameId, periodoInicio, periodoFim);
        return PaginaResponse.de(listar.executar(filtro, pagina, tamanho), ParecerMedicoResponse::de);
    }

    @Operation(summary = "Rejeita alteracao ou exclusao", description = "Parecer e imutavel. Retorna 422; recurso inexistente ou de terceiro retorna 404.")
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void rejeitarAlteracao(@PathVariable UUID id) {
        rejeitarAlteracao.executar(id);
    }
}
