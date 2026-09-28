package br.com.fiap.sus.notificacoes.presentation.response;

import br.com.fiap.sus.notificacoes.application.dto.ContagemNaoLidasOutput;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContagemNaoLidasResponse")
public record ContagemNaoLidasResponse(@Schema(example = "3") long quantidade) {

    public static ContagemNaoLidasResponse of(ContagemNaoLidasOutput o) {
        return new ContagemNaoLidasResponse(o.quantidade());
    }
}
