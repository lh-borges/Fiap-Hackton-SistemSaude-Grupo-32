package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import java.time.Instant;
import java.util.Set;

/** Filtro da linha do tempo por tipos e periodo (RF-04). Periodo inclusivo nas duas pontas. */
public record HistoricoFiltro(Set<TipoRegistro> tipos, Instant inicio, Instant fim) {

    public HistoricoFiltro {
        tipos = tipos == null ? Set.of() : Set.copyOf(tipos);
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Inicio do periodo deve ser igual ou anterior ao fim.");
        }
    }

    public static HistoricoFiltro todos() {
        return new HistoricoFiltro(Set.of(), null, null);
    }

    public boolean aceita(RegistroHistorico registro) {
        if (!tipos.isEmpty() && !tipos.contains(registro.tipo())) {
            return false;
        }
        if (inicio != null && registro.data().isBefore(inicio)) {
            return false;
        }
        return fim == null || !registro.data().isAfter(fim);
    }
}
