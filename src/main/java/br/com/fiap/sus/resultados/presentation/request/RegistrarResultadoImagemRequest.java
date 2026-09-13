package br.com.fiap.sus.resultados.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistrarResultadoImagemRequest(
        @NotNull UUID exameId,
        @NotNull UUID pacienteId,
        @NotBlank String arquivoUrl,
        String descricao,
        String laudo,
        String observacao) {
}