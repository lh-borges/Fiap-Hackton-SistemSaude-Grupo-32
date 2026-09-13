package br.com.fiap.sus.exames.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RegistrarRealizacaoExameDTO(UUID exameId, Instant dataRealizacao) {
}