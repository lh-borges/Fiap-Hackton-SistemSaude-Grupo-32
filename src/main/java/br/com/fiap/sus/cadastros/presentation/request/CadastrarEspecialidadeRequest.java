package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CadastrarEspecialidadeRequest")
public record CadastrarEspecialidadeRequest(
        @Schema(example = "Dermatologia")
        @NotBlank(message = "informe o nome") @Size(min = 3, max = 100) String nome,
        @Schema(example = "Doencas da pele") @Size(max = 255) String descricao) {
}
