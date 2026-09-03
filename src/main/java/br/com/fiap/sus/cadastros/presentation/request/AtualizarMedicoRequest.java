package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(name = "AtualizarMedicoRequest", description = "CRM e UF sao imutaveis apos o cadastro")
public record AtualizarMedicoRequest(@NotNull(message = "informe a especialidade") UUID especialidadeId) {
}
