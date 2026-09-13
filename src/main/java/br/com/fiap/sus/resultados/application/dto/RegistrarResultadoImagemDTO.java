package br.com.fiap.sus.resultados.application.dto;

import java.util.UUID;

public record RegistrarResultadoImagemDTO(UUID exameId, UUID pacienteId, String arquivoUrl,
                                          String descricao, String laudo, String observacao) {
}