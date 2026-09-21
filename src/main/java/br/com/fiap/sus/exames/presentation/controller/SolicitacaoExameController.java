package br.com.fiap.sus.exames.presentation.controller;

import br.com.fiap.sus.exames.application.dto.CancelarSolicitacaoExameDTO;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.application.dto.SolicitarExameDTO;
import br.com.fiap.sus.exames.application.usecase.CancelarSolicitacaoExameUseCase;
import br.com.fiap.sus.exames.application.usecase.ListarSolicitacoesExameUseCase;
import br.com.fiap.sus.exames.application.usecase.SolicitarExameUseCase;
import br.com.fiap.sus.exames.domain.enums.SituacaoSolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameFiltro;
import br.com.fiap.sus.exames.presentation.request.CancelarSolicitacaoExameRequest;
import br.com.fiap.sus.exames.presentation.request.SolicitarExameRequest;
import br.com.fiap.sus.exames.presentation.response.SolicitacaoExameResponse;
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

@Tag(name = "Solicitacoes de Exame")
@RestController
@RequestMapping("/api/v1/solicitacoes-exame")
public class SolicitacaoExameController {

    private final SolicitarExameUseCase solicitar;
    private final CancelarSolicitacaoExameUseCase cancelar;
    private final ListarSolicitacoesExameUseCase listar;

    public SolicitacaoExameController(SolicitarExameUseCase solicitar,
                                      CancelarSolicitacaoExameUseCase cancelar,
                                      ListarSolicitacoesExameUseCase listar) {
        this.solicitar = solicitar;
        this.cancelar = cancelar;
        this.listar = listar;
    }

    @Operation(summary = "Solicita um exame", description = "Exige MEDICO.")
    @PostMapping
    public ResponseEntity<SolicitacaoExameResponse> solicitar(
            @Valid @RequestBody SolicitarExameRequest requisicao) {
        SolicitacaoExameOutput criada = solicitar.executar(new SolicitarExameDTO(requisicao.pacienteId(),
                requisicao.tipoExameId(), requisicao.consultaId(), requisicao.justificativa()));
        return ResponseEntity.created(URI.create("/api/v1/solicitacoes-exame/" + criada.id()))
                .body(SolicitacaoExameResponse.de(criada));
    }

    @Operation(summary = "Cancela uma solicitacao pendente",
            description = "Exige MEDICO (autor) ou ADMINISTRADOR.")
    @PutMapping("/{id}/cancelar")
    public SolicitacaoExameResponse cancelar(@PathVariable UUID id,
                                             @Valid @RequestBody CancelarSolicitacaoExameRequest requisicao) {
        SolicitacaoExameOutput atualizada = cancelar.executar(
                new CancelarSolicitacaoExameDTO(id, requisicao.motivo()));
        return SolicitacaoExameResponse.de(atualizada);
    }

    @Operation(summary = "Lista solicitacoes de exame com filtros")
    @GetMapping
    public PaginaResponse<SolicitacaoExameResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) UUID tipoExameId,
            @RequestParam(required = false) SituacaoSolicitacaoExame situacao,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        SolicitacaoExameFiltro filtro = new SolicitacaoExameFiltro(pacienteId, medicoId, tipoExameId,
                situacao, periodoInicio, periodoFim);
        return PaginaResponse.of(listar.executar(filtro, pagina, tamanho), SolicitacaoExameResponse::de);
    }
}