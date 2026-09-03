package br.com.fiap.sus.cadastros.presentation.response;

import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "Paciente")
public record PacienteResponse(UUID id, UUID usuarioId, String nome, String email, String cartaoSus,
                               LocalDate dataNascimento, String sexo, String tipoSanguineo, boolean ativo) {

    public static PacienteResponse de(PacienteOutput o) {
        return new PacienteResponse(o.id(), o.usuarioId(), o.nome(), o.email(), o.cartaoSus(),
                o.dataNascimento(), o.sexo(), o.tipoSanguineo(), o.ativo());
    }
}
