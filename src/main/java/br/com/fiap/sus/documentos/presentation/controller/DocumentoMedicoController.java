package br.com.fiap.sus.documentos.presentation.controller;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.application.dto.CancelarDocumentoDTO;
import br.com.fiap.sus.documentos.application.dto.DocumentoMedicoOutput;
import br.com.fiap.sus.documentos.application.dto.EmitirDocumentoDTO;
import br.com.fiap.sus.documentos.application.service.DocumentoPdfService;
import br.com.fiap.sus.documentos.application.usecase.CancelarDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.ConsultarDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.EmitirDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.ListarDocumentosUseCase;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoFiltro;
import br.com.fiap.sus.documentos.presentation.request.CancelarDocumentoRequest;
import br.com.fiap.sus.documentos.presentation.request.EmitirDocumentoRequest;
import br.com.fiap.sus.documentos.presentation.response.DocumentoMedicoResponse;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Documentos medicos")
@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoMedicoController {
    private final EmitirDocumentoUseCase emitir;
    private final ConsultarDocumentoUseCase consultar;
    private final ListarDocumentosUseCase listar;
    private final CancelarDocumentoUseCase cancelar;
    private final DocumentoPdfService pdfs;
    private final CadastroQuery cadastros;
    private final ConsultaQuery consultas;
    private final ExameQuery exames;

    public DocumentoMedicoController(
            EmitirDocumentoUseCase emitir,
            ConsultarDocumentoUseCase consultar,
            ListarDocumentosUseCase listar,
            CancelarDocumentoUseCase cancelar,
            DocumentoPdfService pdfs,
            CadastroQuery cadastros,
            ConsultaQuery consultas,
            ExameQuery exames
    ) {
        this.emitir = emitir;
        this.consultar = consultar;
        this.listar = listar;
        this.cancelar = cancelar;
        this.pdfs = pdfs;
        this.cadastros = cadastros;
        this.consultas = consultas;
        this.exames = exames;
    }

    @Operation(summary = "Emite documento medico", description = "Exige MEDICO.")
    @PostMapping
    public ResponseEntity<DocumentoMedicoResponse> emitir(@Valid @RequestBody EmitirDocumentoRequest request) {
        var criado = emitir.executar(new EmitirDocumentoDTO(request.pacienteId(), request.tipo(),
                request.conteudo(), request.consultaId(), request.exameId()));
        return ResponseEntity.created(URI.create("/api/v1/documentos/" + criado.id()))
                .body(resposta(criado));
    }

    @Operation(summary = "Consulta documento medico")
    @GetMapping("/{id}")
    public DocumentoMedicoResponse consultar(@PathVariable UUID id) {
        return resposta(consultar.executar(id));
    }

    @Operation(summary = "Baixa documento medico em PDF", description = "Gera PDF via JasperReports.")
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdf(@PathVariable UUID id) {
        var documento = consultar.executar(id);
        byte[] bytes = pdfs.gerar(documento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("documento-" + id + ".pdf")
                        .build()
                        .toString())
                .body(bytes);
    }

    @Operation(summary = "Lista documentos medicos", description = "A listagem retorna metadados sem conteudo.")
    @GetMapping
    public PaginaResponse<DocumentoMedicoResponse> listar(
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) TipoDocumento tipo,
            @RequestParam(required = false) Instant periodoInicio,
            @RequestParam(required = false) Instant periodoFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho
    ) {
        var filtro = new DocumentoMedicoFiltro(pacienteId, medicoId, tipo, periodoInicio, periodoFim);
        return PaginaResponse.of(listar.executar(filtro, pagina, tamanho), this::resposta);
    }

    @Operation(summary = "Cancela documento medico", description = "Exige MEDICO autor.")
    @PutMapping("/{id}/cancelar")
    public DocumentoMedicoResponse cancelar(@PathVariable UUID id,
                                            @Valid @RequestBody CancelarDocumentoRequest request) {
        return resposta(cancelar.executar(new CancelarDocumentoDTO(id, request.motivo())));
    }

    @Operation(summary = "Rejeita alteracao ou exclusao", description = "Documento emitido e imutavel.")
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void rejeitarAlteracao(@PathVariable UUID id) {
        throw new RegraDeNegocioException(
                "Documento medico e imutavel e nao pode ser alterado ou excluido. Emita um novo documento ou cancele o atual.");
    }

    private DocumentoMedicoResponse resposta(DocumentoMedicoOutput output) {
        var paciente = cadastros.resumoDoPaciente(output.pacienteId()).orElse(null);
        var medico = cadastros.resumoDoMedico(output.medicoId()).orElse(null);
        var dataConsulta = output.consultaId() == null
                ? null
                : consultas.dataHoraDaConsulta(output.consultaId()).orElse(null);
        boolean laudoComExame = output.tipo() == TipoDocumento.LAUDO && output.exameId() != null;
        var dataRealizacaoExame = !laudoComExame
                ? null
                : exames.dataRealizacaoDoExame(output.exameId()).orElse(null);
        UUID tipoExameId = !laudoComExame
                ? null
                : exames.tipoExameIdDoExame(output.exameId()).orElse(null);
        String tipoExameNome = tipoExameId == null
                ? null
                : cadastros.nomeDoTipoExame(tipoExameId).orElse(null);
        return DocumentoMedicoResponse.de(output, paciente, medico, dataConsulta, dataRealizacaoExame, tipoExameNome);
    }
}
