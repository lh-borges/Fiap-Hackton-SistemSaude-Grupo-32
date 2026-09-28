package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.List;

/** Colecao ordenada dos registros de um paciente (RN-02, RN-07), com filtro e paginacao em memoria. */
public final class LinhaDoTempo {
    public static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final List<RegistroHistorico> registros;

    private LinhaDoTempo(List<RegistroHistorico> registrosOrdenados) {
        this.registros = registrosOrdenados;
    }

    public static LinhaDoTempo de(List<RegistroHistorico> registros) {
        return new LinhaDoTempo(registros.stream().sorted(RegistroHistorico.ORDEM).toList());
    }

    public LinhaDoTempo filtrar(HistoricoFiltro filtro) {
        return new LinhaDoTempo(registros.stream().filter(filtro::aceita).toList());
    }

    public PaginaResultado<RegistroHistorico> pagina(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO_PAGINA) {
            throw new IllegalArgumentException(
                    "Pagina deve ser positiva ou zero e tamanho entre 1 e " + TAMANHO_MAXIMO_PAGINA + ".");
        }
        int inicio = Math.min(pagina * tamanho, registros.size());
        int fim = Math.min(inicio + tamanho, registros.size());
        return PaginaResultado.de(registros.subList(inicio, fim), pagina, tamanho, registros.size());
    }

    public List<RegistroHistorico> registros() {
        return registros;
    }

    public int total() {
        return registros.size();
    }
}
