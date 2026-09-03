package br.com.fiap.sus.cadastros.application.dto;

import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import java.util.UUID;

public record EspecialidadeOutput(UUID id, String nome, String descricao, boolean ativo) {

    public static EspecialidadeOutput de(Especialidade especialidade) {
        return new EspecialidadeOutput(especialidade.getId(), especialidade.getNome(),
                especialidade.getDescricao(), especialidade.isAtivo());
    }
}
