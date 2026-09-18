package br.com.fiap.sus.pareceres.application.dto;

import java.util.UUID;

public record RegistrarParecerDTO(
        UUID resultadoExameId,
        String descricao
) {
}
