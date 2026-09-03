package br.com.fiap.sus.cadastros.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CadastrarPacienteInput(UUID usuarioId, String cartaoSus, LocalDate dataNascimento,
                                     String sexo, String tipoSanguineo) {
}
