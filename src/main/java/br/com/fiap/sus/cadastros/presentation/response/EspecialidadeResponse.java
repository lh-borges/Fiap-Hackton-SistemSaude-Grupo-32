package br.com.fiap.sus.cadastros.presentation.response;

import br.com.fiap.sus.cadastros.application.dto.EspecialidadeOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "Especialidade")
public record EspecialidadeResponse(UUID id, String nome, String descricao, boolean ativo) {

    public static EspecialidadeResponse de(EspecialidadeOutput o) {
        return new EspecialidadeResponse(o.id(), o.nome(), o.descricao(), o.ativo());
    }
}
