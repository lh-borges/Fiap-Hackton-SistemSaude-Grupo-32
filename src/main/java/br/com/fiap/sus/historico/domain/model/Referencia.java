package br.com.fiap.sus.historico.domain.model;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import java.util.Objects;
import java.util.UUID;

/** Aponta o registro relacionado a um item do historico (RF-07). */
public record Referencia(TipoRegistro tipo, UUID id) {

    public Referencia {
        Objects.requireNonNull(tipo, "Tipo da referencia e obrigatorio.");
        Objects.requireNonNull(id, "Id da referencia e obrigatorio.");
    }

    /** Referencia opcional: devolve null quando nao ha registro relacionado. */
    public static Referencia opcional(TipoRegistro tipo, UUID id) {
        return id == null ? null : new Referencia(tipo, id);
    }
}
