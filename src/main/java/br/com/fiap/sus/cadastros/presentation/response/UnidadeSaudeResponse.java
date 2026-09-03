package br.com.fiap.sus.cadastros.presentation.response;

import br.com.fiap.sus.cadastros.application.dto.UnidadeSaudeOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "UnidadeSaude")
public record UnidadeSaudeResponse(UUID id, String nome, String cnes, String telefone, boolean ativo) {

    public static UnidadeSaudeResponse de(UnidadeSaudeOutput o) {
        return new UnidadeSaudeResponse(o.id(), o.nome(), o.cnes(), o.telefone(), o.ativo());
    }
}
