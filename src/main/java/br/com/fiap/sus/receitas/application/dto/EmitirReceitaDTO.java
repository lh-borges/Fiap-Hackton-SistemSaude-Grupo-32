package br.com.fiap.sus.receitas.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmitirReceitaDTO(UUID pacienteId, UUID consultaId, List<ItemReceitaDTO> itens,
                               Instant validade, String observacao) {
}