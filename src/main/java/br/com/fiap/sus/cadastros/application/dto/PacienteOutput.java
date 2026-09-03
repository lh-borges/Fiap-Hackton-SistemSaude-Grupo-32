package br.com.fiap.sus.cadastros.application.dto;

import br.com.fiap.sus.cadastros.domain.model.Paciente;
import java.time.LocalDate;
import java.util.UUID;

public record PacienteOutput(UUID id, UUID usuarioId, String nome, String email, String cartaoSus,
                             LocalDate dataNascimento, String sexo, String tipoSanguineo, boolean ativo) {

    public static PacienteOutput de(Paciente paciente, String nome, String email) {
        return new PacienteOutput(paciente.getId(), paciente.getUsuarioId(), nome, email,
                paciente.getCartaoSus(), paciente.getDataNascimento(), paciente.getSexo().name(),
                paciente.getTipoSanguineo(), paciente.isAtivo());
    }
}
