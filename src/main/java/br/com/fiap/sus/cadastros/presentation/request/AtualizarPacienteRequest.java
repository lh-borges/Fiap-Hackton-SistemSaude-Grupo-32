package br.com.fiap.sus.cadastros.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Schema(name = "AtualizarPacienteRequest")
public record AtualizarPacienteRequest(
        @NotNull(message = "informe a data de nascimento")
        @Past(message = "a data de nascimento deve estar no passado") LocalDate dataNascimento,
        @NotBlank(message = "informe o sexo") String sexo,
        String tipoSanguineo) {
}
