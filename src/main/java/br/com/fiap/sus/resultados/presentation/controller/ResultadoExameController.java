package br.com.fiap.sus.resultados.presentation.controller;

import br.com.fiap.sus.resultados.application.dto.ItemResultadoLaboratorialDTO;
import br.com.fiap.sus.resultados.application.dto.RegistrarResultadoImagemDTO;
import br.com.fiap.sus.resultados.application.dto.RegistrarResultadoLaboratorialDTO;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.application.usecase.ConsultarResultadoUseCase;
import br.com.fiap.sus.resultados.application.usecase.ListarResultadosUseCase;
import br.com.fiap.sus.resultados.application.usecase.RegistrarResultadoImagemUseCase;
import br.com.fiap.sus.resultados.application.usecase.RegistrarResultadoLaboratorialUseCase;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameFiltro;
import br.com.fiap.sus.resultados.presentation.request.RegistrarResultadoImagemRequest;
import br.com.fiap.sus.resultados.presentation.request.RegistrarResultadoLaboratorialRequest;
import br.com.fiap.sus.resultados.presentation.response.ResultadoExameResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Resultados de Exame")
@RestController
@RequestMapping("/api/v1/resultados-exame")
public class ResultadoExameController {

    private final RegistrarResultadoImagemUseCase registrarImagem;
    private final RegistrarResultadoLaboratorialUseCase registrarLaboratorial;
    private final ConsultarResultadoUseCase consultar;
    private final ListarResultadosUseCase listar;

    public ResultadoExameController(RegistrarResultadoImagemUseCase registrarImagem,
                                    RegistrarResultadoLaboratorialUseCase registrarLaboratorial,
                                    ConsultarResultadoUseCase consultar, ListarResultadosUseCase listar) {
        this.registrarImagem = registrarImagem;
        this.registrarLaboratorial = registrarLaboratorial;
        this.consultar = consultar;
        this.listar = listar;
    }

    @Operation(summary = "Registra resultado de imagem", description = "Exige ATENDENTE ou ADMINISTRADOR.")
    @PostMapping("/imagem")
    public ResponseEntity<ResultadoExameResponse> registrarImagem(
            @Valid @RequestBody RegistrarResultadoImagemRequest requisicao) {
        ResultadoExameOutput criado = registrarImagem.executar(new RegistrarResultadoImagemDTO(
                requisicao.exameId(), requisicao.pacienteId(), requisicao.arquivoUrl(),
                requisicao.descricao(), requisicao.laudo(), requisicao.observacao()));
        return ResponseEntity.created(URI.create("/api/v1/resultados-exame/" + criado.id()))
                .body(ResultadoExameResponse.de(criado));
    }

    @Operation(summary = "Registra resultado laboratorial", description = "Exige ATENDENTE ou ADMINISTRADOR.")
    @PostMapping("/laboratorial")
    public ResponseEntity<ResultadoExameResponse> registrarLaboratorial(
            @Valid @RequestBody RegistrarResultadoLaboratorialRequest requisicao) {
        var itens = requisicao.itens().stream()
                .map(i -> new ItemResultadoLaboratorialDTO(i.nomeParametro(), i.valor(), i.unidade(),
                        i.valorMinimoReferencia(), i.valorMaximoReferencia(), i.resultadoTexto(),
                        i.situacao()))
                .toList();
        ResultadoExameOutput criado = registrarLaboratorial.executar(new RegistrarResultadoLaboratorialDTO(
                requisicao.exameId(), requisicao.pacienteId(), itens, requisicao.observacao()));
        return ResponseEntity.created(URI.create("/api/v1/resultados-exame/" + criado.id()))
                .body(ResultadoExameResponse.de(criado));
    }

    @Operation(summary = "Consulta um resultado", description = "PACIENTE so acessa os proprios (404 caso contrario).")
    @GetMapping("/{id}")
    public ResultadoExameResponse consultar(@PathVariable UUID id) {
        return ResultadoExameResponse.de(consultar.executar(id));
    }

    @Operation(summary = "Lista resultados com filtros", description = "PACIENTE ve apenas os proprios.")
    @GetMapping
    public PaginaResponse<ResultadoExameResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        ResultadoExameFiltro filtro = new ResultadoExameFiltro(pacienteId, periodoInicio, periodoFim);
        return PaginaResponse.de(listar.executar(filtro, pagina, tamanho), ResultadoExameResponse::de);
    }
}