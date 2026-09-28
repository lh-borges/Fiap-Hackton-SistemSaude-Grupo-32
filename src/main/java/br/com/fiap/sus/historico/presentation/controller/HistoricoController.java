package br.com.fiap.sus.historico.presentation.controller;

import br.com.fiap.sus.historico.application.usecase.ConsultarHistoricoUseCase;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.HistoricoFiltro;
import br.com.fiap.sus.historico.presentation.response.RegistroHistoricoResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Historico", description = "Linha do tempo do atendimento do paciente, derivada dos registros existentes")
@RestController
@RequestMapping("/api/v1/historico")
public class HistoricoController {

    private final ConsultarHistoricoUseCase consultar;

    public HistoricoController(ConsultarHistoricoUseCase consultar) {
        this.consultar = consultar;
    }

    @Operation(summary = "Linha do tempo do paciente, do registro mais recente para o mais antigo",
            description = "PACIENTE ve o proprio historico (pacienteId ignorado). MEDICO informa pacienteId e so "
                    + "enxerga pacientes com quem tem consulta nao cancelada. ADMINISTRADOR informa qualquer "
                    + "pacienteId. ATENDENTE nao acessa. Cada item aponta o recurso e o id do registro de origem; "
                    + "o conteudo clinico e lido na rota do modulo dono.")
    @GetMapping
    public PaginaResponse<RegistroHistoricoResponse> consultar(
            @Parameter(description = "Obrigatorio para MEDICO e ADMINISTRADOR; ignorado para PACIENTE")
            @RequestParam(required = false) UUID pacienteId,
            @Parameter(description = "Um ou mais tipos de registro; omitido devolve todos")
            @RequestParam(required = false) Set<TipoRegistro> tipo,
            @Parameter(description = "Inicio do periodo (inclusivo), ISO-8601")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @Parameter(description = "Fim do periodo (inclusivo), ISO-8601")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        var filtro = new HistoricoFiltro(tipo, inicio, fim);
        return PaginaResponse.of(consultar.executar(pacienteId, filtro, pagina, tamanho), RegistroHistoricoResponse::of);
    }
}
