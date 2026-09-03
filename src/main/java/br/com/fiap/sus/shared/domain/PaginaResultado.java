package br.com.fiap.sus.shared.domain;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado independente de framework, para que as portas de repositorio
 * declaradas no dominio nao dependam do Spring Data (Artigo II.2).
 */
public record PaginaResultado<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T> PaginaResultado<T> de(List<T> conteudo, int pagina, int tamanho, long totalElementos) {
        int totalPaginas = tamanho <= 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanho);
        return new PaginaResultado<>(List.copyOf(conteudo), pagina, tamanho, totalElementos, totalPaginas);
    }

    public <R> PaginaResultado<R> mapear(Function<T, R> conversor) {
        return new PaginaResultado<>(conteudo.stream().map(conversor).toList(),
                pagina, tamanho, totalElementos, totalPaginas);
    }
}
