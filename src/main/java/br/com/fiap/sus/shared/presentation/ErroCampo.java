package br.com.fiap.sus.shared.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErroCampo", description = "Detalhe de um campo que falhou na validacao")
public record ErroCampo(
        @Schema(example = "email") String campo,
        @Schema(example = "deve ser um endereco de e-mail bem formado") String mensagem) {
}
