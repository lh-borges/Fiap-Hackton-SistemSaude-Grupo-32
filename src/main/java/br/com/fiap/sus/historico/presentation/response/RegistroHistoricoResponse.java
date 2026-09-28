package br.com.fiap.sus.historico.presentation.response;

import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import java.time.Instant;
import java.util.UUID;

public record RegistroHistoricoResponse(
        TipoRegistro tipo,
        UUID id,
        String recurso,
        Instant data,
        String titulo,
        String situacao,
        UUID medicoId,
        ReferenciaResponse origem
) {
    public static RegistroHistoricoResponse of(RegistroHistorico r) {
        return new RegistroHistoricoResponse(r.tipo(), r.id(), r.recurso(), r.data(), r.titulo(), r.situacao(),
                r.medicoId(), ReferenciaResponse.of(r.origem()));
    }
}
