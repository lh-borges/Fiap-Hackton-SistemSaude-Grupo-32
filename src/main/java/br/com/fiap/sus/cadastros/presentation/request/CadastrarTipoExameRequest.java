package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CadastrarTipoExameRequest")
public record CadastrarTipoExameRequest(
        @Schema(example = "Ressonancia magnetica")
        @NotBlank(message = "informe o nome") @Size(min = 3, max = 150) String nome,
        @Schema(example = "IMAGEM", description = "IMAGEM ou LABORATORIAL")
        @NotBlank(message = "informe a categoria") String categoria,
        @Schema(example = "Retirar objetos metalicos e informar implantes.") String preparo) {
}
