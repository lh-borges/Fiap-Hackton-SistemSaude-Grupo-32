package br.com.fiap.sus.receitas.presentation.controller;

import br.com.fiap.sus.receitas.application.dto.CancelarReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.EmitirReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ItemReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.application.dto.RenovarReceitaDTO;
import br.com.fiap.sus.receitas.application.usecase.CancelarReceitaUseCase;
import br.com.fiap.sus.receitas.application.usecase.EmitirReceitaUseCase;
import br.com.fiap.sus.receitas.application.usecase.ListarReceitasUseCase;
import br.com.fiap.sus.receitas.application.usecase.RenovarReceitaUseCase;
import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaFiltro;
import br.com.fiap.sus.receitas.presentation.request.CancelarReceitaRequest;
import br.com.fiap.sus.receitas.presentation.request.EmitirReceitaRequest;
import br.com.fiap.sus.receitas.presentation.request.RenovarReceitaRequest;
import br.com.fiap.sus.receitas.presentation.response.ReceitaResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
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

@Tag(name = "Receitas")
@RestController
@RequestMapping("/api/v1/receitas")
public class ReceitaController {

    private final EmitirReceitaUseCase emitir;
    private final RenovarReceitaUseCase renovar;
    private final CancelarReceitaUseCase cancelar;
    private final ListarReceitasUseCase listar;

    public ReceitaController(EmitirReceitaUseCase emitir, RenovarReceitaUseCase renovar,
                             CancelarReceitaUseCase cancelar, ListarReceitasUseCase listar) {
        this.emitir = emitir;
        this.renovar = renovar;
        this.cancelar = cancelar;
        this.listar = listar;
    }

    @Operation(summary = "Emite uma receita", description = "Exige MEDICO.")
    @PostMapping
    public ResponseEntity<ReceitaResponse> emitir(@Valid @RequestBody EmitirReceitaRequest requisicao) {
        var itens = requisicao.itens().stream()
                .map(i -> new ItemReceitaDTO(i.medicamento(), i.dosagem(), i.frequencia(), i.duracao(),
                        i.orientacao()))
                .toList();
        ReceitaOutput criada = emitir.executar(new EmitirReceitaDTO(requisicao.pacienteId(),
                requisicao.consultaId(), itens, requisicao.validade(), requisicao.observacao()));
        return ResponseEntity.created(URI.create("/api/v1/receitas/" + criada.id()))
                .body(ReceitaResponse.de(criada));
    }

    @Operation(summary = "Renova uma receita ativa", description = "Cria uma nova receita; exige MEDICO.")
    @PostMapping("/{id}/renovar")
    public ResponseEntity<ReceitaResponse> renovar(@PathVariable UUID id,
                                                   @Valid @RequestBody RenovarReceitaRequest requisicao) {
        ReceitaOutput renovada = renovar.executar(new RenovarReceitaDTO(id, requisicao.novaConsultaId(),
                requisicao.novaValidade(), requisicao.novaObservacao()));
        return ResponseEntity.created(URI.create("/api/v1/receitas/" + renovada.id()))
                .body(ReceitaResponse.de(renovada));
    }

    @Operation(summary = "Cancela uma receita", description = "Exige o MEDICO autor.")
    @PutMapping("/{id}/cancelar")
    public ReceitaResponse cancelar(@PathVariable UUID id,
                                    @Valid @RequestBody CancelarReceitaRequest requisicao) {
        ReceitaOutput atualizada = cancelar.executar(new CancelarReceitaDTO(id, requisicao.motivo()));
        return ReceitaResponse.de(atualizada);
    }

    @Operation(summary = "Lista receitas com filtros", description = "PACIENTE ve apenas as proprias.")
    @GetMapping
    public PaginaResponse<ReceitaResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) SituacaoReceita situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho) {
        ReceitaFiltro filtro = new ReceitaFiltro(pacienteId, medicoId, situacao);
        return PaginaResponse.de(listar.executar(filtro, pagina, tamanho), ReceitaResponse::de);
    }
}