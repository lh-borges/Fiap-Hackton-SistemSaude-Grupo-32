package br.com.fiap.sus.pareceres.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(name = "RegistrarParecerRequest")
public record RegistrarParecerRequest(

        @NotNull(message = "O resultado do exame é obrigatório.")
        @Schema(description = "Identificador do resultado a ser interpretado")
        UUID resultadoExameId,

        @NotBlank(message = "A descricao do parecer é obrigatória.")
        @Size(min = 10, max = 5000, message = "A descricao deve ter entre 10 e 5000 caracteres.")
        @Schema(description = "Interpretacao clinica do medico", minLength = 10, maxLength = 5000)
        String descricao
) {
}
