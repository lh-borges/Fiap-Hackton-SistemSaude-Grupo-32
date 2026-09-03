package br.com.fiap.sus.cadastros.application.dto;

import java.time.LocalDate;

public record AtualizarPacienteInput(LocalDate dataNascimento, String sexo, String tipoSanguineo) {
}
