package br.com.fiap.sus.resultados.application.dto;

import java.util.List;
import java.util.UUID;

public record RegistrarResultadoLaboratorialDTO(UUID exameId, UUID pacienteId,
                                                List<ItemResultadoLaboratorialDTO> itens, String observacao) {
}