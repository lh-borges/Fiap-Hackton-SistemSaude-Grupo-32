package br.com.fiap.sus.iam.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "AlterarSenhaRequest",
        description = "A senha atual e obrigatoria quando o proprio usuario troca a senha")
public record AlterarSenhaRequest(
        String senhaAtual,
        @NotBlank(message = "informe a nova senha") String novaSenha) {
}
