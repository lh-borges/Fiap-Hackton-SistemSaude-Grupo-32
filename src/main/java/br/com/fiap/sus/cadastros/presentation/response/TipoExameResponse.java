package br.com.fiap.sus.cadastros.presentation.response;

import br.com.fiap.sus.cadastros.application.dto.TipoExameOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "TipoExame")
public record TipoExameResponse(UUID id, String nome, String categoria, String preparo, boolean ativo) {

    public static TipoExameResponse de(TipoExameOutput o) {
        return new TipoExameResponse(o.id(), o.nome(), o.categoria(), o.preparo(), o.ativo());
    }
}
