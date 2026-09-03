package br.com.fiap.sus.cadastros.application.dto;

import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import java.util.UUID;

public record TipoExameOutput(UUID id, String nome, String categoria, String preparo, boolean ativo) {

    public static TipoExameOutput de(TipoExame tipoExame) {
        return new TipoExameOutput(tipoExame.getId(), tipoExame.getNome(),
                tipoExame.getCategoria().name(), tipoExame.getPreparo(), tipoExame.isAtivo());
    }
}
