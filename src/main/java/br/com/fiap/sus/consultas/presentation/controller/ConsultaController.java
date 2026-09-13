package br.com.fiap.sus.consultas.presentation.controller;

import br.com.fiap.sus.consultas.application.dto.AgendarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.CancelarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RegistrarRealizacaoConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.RemarcarConsultaDTO;
import br.com.fiap.sus.consultas.application.usecase.AgendarConsultaUseCase;
import br.com.fiap.sus.consultas.application.usecase.CancelarConsultaUseCase;
import br.com.fiap.sus.consultas.application.usecase.ListarConsultasUseCase;
import br.com.fiap.sus.consultas.application.usecase.RegistrarRealizacaoConsultaUseCase;
import br.com.fiap.sus.consultas.application.usecase.RemarcarConsultaUseCase;
import br.com.fiap.sus.consultas.domain.enums.SituacaoConsulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaFiltro;
import br.com.fiap.sus.consultas.presentation.request.AgendarConsultaRequest;
import br.com.fiap.sus.consultas.presentation.request.CancelarConsultaRequest;
import br.com.fiap.sus.consultas.presentation.request.RegistrarRealizacaoConsultaRequest;
import br.com.fiap.sus.consultas.presentation.request.RemarcarConsultaRequest;
import br.com.fiap.sus.consultas.presentation.response.ConsultaResponse;
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

@Tag(name = "Consultas")
@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaController {

    private final AgendarConsultaUseCase agendar;
    private final RemarcarConsultaUseCase remarcar;
    private final CancelarConsultaUseCase cancelar;
    private final RegistrarRealizacaoConsultaUseCase registrarRealizacao;
    private final ListarConsultasUseCase listar;

    public ConsultaController(AgendarConsultaUseCase agendar, RemarcarConsultaUseCase remarcar,
                              CancelarConsultaUseCase cancelar,
                              RegistrarRealizacaoConsultaUseCase registrarRealizacao,
                              ListarConsultasUseCase listar) {
        this.agendar = agendar;
        this.remarcar = remarcar;
        this.cancelar = cancelar;
        this.registrarRealizacao = registrarRealizacao;
        this.listar = listar;
    }

    @Operation(summary = "Agenda uma consulta", description = "ATENDENTE/ADMINISTRADOR para qualquer paciente; PACIENTE apenas para si mesmo.")
    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody AgendarConsultaRequest requisicao) {
        ConsultaOutput criada = agendar.executar(new AgendarConsultaDTO(requisicao.pacienteId(),
                requisicao.medicoId(), requisicao.unidadeSaudeId(), requisicao.dataHora(),
                requisicao.motivo()));
        return ResponseEntity.created(URI.create("/api/v1/consultas/" + criada.id()))
                .body(ConsultaResponse.de(criada));
    }

    @Operation(summary = "Remarca uma consulta agendada")
    @PutMapping("/{id}/remarcar")
    public ConsultaResponse remarcar(@PathVariable UUID id,
                                     @Valid @RequestBody RemarcarConsultaRequest requisicao) {
        ConsultaOutput atualizada = remarcar.executar(new RemarcarConsultaDTO(id, requisicao.novaDataHora()));
        return ConsultaResponse.de(atualizada);
    }

    @Operation(summary = "Cancela uma consulta", description = "Exige motivo.")
    @PutMapping("/{id}/cancelar")
    public ConsultaResponse cancelar(@PathVariable UUID id,
                                     @Valid @RequestBody CancelarConsultaRequest requisicao) {
        ConsultaOutput atualizada = cancelar.executar(new CancelarConsultaDTO(id,
                requisicao.motivoCancelamento()));
        return ConsultaResponse.de(atualizada);
    }

    @Operation(summary = "Registra a realizacao de uma consulta", description = "Exige MEDICO responsavel pela consulta.")
    @PutMapping("/{id}/realizar")
    public ConsultaResponse registrarRealizacao(@PathVariable UUID id,
                                                @RequestBody RegistrarRealizacaoConsultaRequest requisicao) {
        ConsultaOutput atualizada = registrarRealizacao.executar(
                new RegistrarRealizacaoConsultaDTO(id, requisicao.observacoes()));
        return ConsultaResponse.de(atualizada);
    }

    @Operation(summary = "Lista consultas com filtros", description = "PACIENTE ve apenas as proprias, independente do filtro informado.")
    @GetMapping
    public PaginaResponse<ConsultaResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) UUID unidadeSaudeId,
            @RequestParam(required = false) SituacaoConsulta situacao,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        ConsultaFiltro filtro = new ConsultaFiltro(pacienteId, medicoId, unidadeSaudeId, situacao,
                periodoInicio, periodoFim);
        return PaginaResponse.de(listar.executar(filtro, pagina, tamanho), ConsultaResponse::de);
    }
}