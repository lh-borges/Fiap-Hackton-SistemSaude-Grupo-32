package br.com.fiap.sus.cadastros.application.dto;

import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import java.util.UUID;

public record UnidadeSaudeOutput(UUID id, String nome, String cnes, String telefone, boolean ativo) {

    public static UnidadeSaudeOutput de(UnidadeSaude unidade) {
        return new UnidadeSaudeOutput(unidade.getId(), unidade.getNome(), unidade.getCnes(),
                unidade.getTelefone(), unidade.isAtivo());
    }
}
