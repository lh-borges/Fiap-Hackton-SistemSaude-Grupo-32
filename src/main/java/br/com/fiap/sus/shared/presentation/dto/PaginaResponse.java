package br.com.fiap.sus.shared.presentation.dto;

import br.com.fiap.sus.shared.domain.PaginaResultado;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

@Schema(name = "Pagina", description = "Envelope padrao de listagem paginada")
public record PaginaResponse<T>(
        @Schema(description = "Itens da pagina atual") List<T> conteudo,
        @Schema(description = "Indice da pagina, iniciando em zero", example = "0") int pagina,
        @Schema(description = "Quantidade de itens por pagina", example = "20") int tamanho,
        @Schema(description = "Total de itens encontrados", example = "42") long totalElementos,
        @Schema(description = "Total de paginas", example = "3") int totalPaginas) {

    public static <D, R> PaginaResponse<R> de(PaginaResultado<D> pagina, Function<D, R> conversor) {
        return new PaginaResponse<>(pagina.conteudo().stream().map(conversor).toList(),
                pagina.pagina(), pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas());
    }
}
