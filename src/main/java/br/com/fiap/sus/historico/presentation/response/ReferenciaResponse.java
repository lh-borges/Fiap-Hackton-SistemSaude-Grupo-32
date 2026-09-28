package br.com.fiap.sus.historico.presentation.response;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import java.util.UUID;

public record ReferenciaResponse(TipoRegistro tipo, String recurso, UUID id) {

    public static ReferenciaResponse of(Referencia r) {
        return r == null ? null : new ReferenciaResponse(r.tipo(), r.tipo().recurso(), r.id());
    }
}
