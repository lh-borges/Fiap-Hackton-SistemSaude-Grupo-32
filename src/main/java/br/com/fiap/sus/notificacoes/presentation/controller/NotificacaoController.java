package br.com.fiap.sus.notificacoes.presentation.controller;

import br.com.fiap.sus.notificacoes.application.usecase.ConsultarMinhaNotificacaoUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.ContarNaoLidasUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.ListarMinhasNotificacoesUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.MarcarNotificacaoComoLidaUseCase;
import br.com.fiap.sus.notificacoes.presentation.response.ContagemNaoLidasResponse;
import br.com.fiap.sus.notificacoes.presentation.response.NotificacaoResponse;
import br.com.fiap.sus.shared.presentation.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Contrato em specs/009-notificacoes/contracts/openapi.yaml. Toda rota opera sobre o usuario do token. */
@Tag(name = "Notificacoes", description = "Avisos do proprio usuario sobre fatos do seu atendimento")
@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {

    private final ListarMinhasNotificacoesUseCase listar;
    private final ConsultarMinhaNotificacaoUseCase consultar;
    private final MarcarNotificacaoComoLidaUseCase marcarComoLida;
    private final ContarNaoLidasUseCase contarNaoLidas;

    public NotificacaoController(ListarMinhasNotificacoesUseCase listar, ConsultarMinhaNotificacaoUseCase consultar,
                                 MarcarNotificacaoComoLidaUseCase marcarComoLida, ContarNaoLidasUseCase contarNaoLidas) {
        this.listar = listar;
        this.consultar = consultar;
        this.marcarComoLida = marcarComoLida;
        this.contarNaoLidas = contarNaoLidas;
    }

    @Operation(summary = "Lista minhas notificacoes",
            description = "Da mais recente para a mais antiga. Qualquer perfil autenticado; so enxerga as proprias.")
    @GetMapping
    public PaginaResponse<NotificacaoResponse> listar(
            @Parameter(description = "true: so lidas; false: so nao lidas; omitido: todas")
            @RequestParam(required = false) Boolean lida,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.of(listar.executar(lida, pagina, tamanho), NotificacaoResponse::of);
    }

    @Operation(summary = "Quantidade de notificacoes nao lidas")
    @GetMapping("/nao-lidas/contagem")
    public ContagemNaoLidasResponse contarNaoLidas() {
        return ContagemNaoLidasResponse.of(contarNaoLidas.executar());
    }

    @Operation(summary = "Consulta uma notificacao minha",
            description = "Notificacao de outro usuario responde 404.")
    @GetMapping("/{id}")
    public NotificacaoResponse consultar(@PathVariable UUID id) {
        return NotificacaoResponse.of(consultar.executar(id));
    }

    @Operation(summary = "Marca uma notificacao minha como lida",
            description = "Irreversivel e idempotente: se ja estava lida, responde 200 sem alterar a data de leitura.")
    @PatchMapping("/{id}/leitura")
    public NotificacaoResponse marcarComoLida(@PathVariable UUID id) {
        return NotificacaoResponse.of(marcarComoLida.executar(id));
    }
}
