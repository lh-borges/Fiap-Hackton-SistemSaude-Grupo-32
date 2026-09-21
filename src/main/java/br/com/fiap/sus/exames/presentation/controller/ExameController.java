package br.com.fiap.sus.exames.presentation.controller;

import br.com.fiap.sus.exames.application.dto.AgendarExameDTO;
import br.com.fiap.sus.exames.application.dto.CancelarExameDTO;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.application.dto.RegistrarRealizacaoExameDTO;
import br.com.fiap.sus.exames.application.usecase.AgendarExameUseCase;
import br.com.fiap.sus.exames.application.usecase.CancelarExameUseCase;
import br.com.fiap.sus.exames.application.usecase.ListarExamesUseCase;
import br.com.fiap.sus.exames.application.usecase.RegistrarRealizacaoExameUseCase;
import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameFiltro;
import br.com.fiap.sus.exames.presentation.request.AgendarExameRequest;
import br.com.fiap.sus.exames.presentation.request.RegistrarRealizacaoExameRequest;
import br.com.fiap.sus.exames.presentation.response.ExameResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exames")
@RestController
@RequestMapping("/api/v1/exames")
public class ExameController {

    private final AgendarExameUseCase agendar;
    private final RegistrarRealizacaoExameUseCase registrarRealizacao;
    private final CancelarExameUseCase cancelar;
    private final ListarExamesUseCase listar;

    public ExameController(AgendarExameUseCase agendar, RegistrarRealizacaoExameUseCase registrarRealizacao,
                           CancelarExameUseCase cancelar, ListarExamesUseCase listar) {
        this.agendar = agendar;
        this.registrarRealizacao = registrarRealizacao;
        this.cancelar = cancelar;
        this.listar = listar;
    }

    @Operation(summary = "Agenda um exame a partir de uma solicitacao pendente",
            description = "Exige ATENDENTE ou ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<ExameResponse> agendar(
            @RequestParam UUID solicitacaoExameId,
            @Valid @RequestBody AgendarExameRequest requisicao) {
        ExameOutput criado = agendar.executar(new AgendarExameDTO(solicitacaoExameId,
                requisicao.unidadeSaudeId(), requisicao.dataAgendada()));
        return ResponseEntity.created(URI.create("/api/v1/exames/" + criado.id()))
                .body(ExameResponse.de(criado));
    }

    @Operation(summary = "Registra a realizacao de um exame agendado",
            description = "Exige ATENDENTE ou ADMINISTRADOR.")
    @PutMapping("/{id}/realizar")
    public ExameResponse registrarRealizacao(@PathVariable UUID id,
                                             @Valid @RequestBody RegistrarRealizacaoExameRequest requisicao) {
        ExameOutput atualizado = registrarRealizacao.executar(
                new RegistrarRealizacaoExameDTO(id, requisicao.dataRealizacao()));
        return ExameResponse.de(atualizado);
    }

    @Operation(summary = "Cancela um exame agendado",
            description = "A solicitacao de origem volta a situacao pendente. Exige ATENDENTE ou ADMINISTRADOR.")
    @PutMapping("/{id}/cancelar")
    public ExameResponse cancelar(@PathVariable UUID id) {
        ExameOutput atualizado = cancelar.executar(new CancelarExameDTO(id));
        return ExameResponse.de(atualizado);
    }

    @Operation(summary = "Lista exames com filtros", description = "PACIENTE ve apenas os proprios.")
    @GetMapping
    public PaginaResponse<ExameResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID unidadeSaudeId,
            @RequestParam(required = false) SituacaoExame situacao,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        ExameFiltro filtro = new ExameFiltro(pacienteId, unidadeSaudeId, situacao, periodoInicio, periodoFim);
        return PaginaResponse.of(listar.executar(filtro, pagina, tamanho), ExameResponse::de);
    }
}