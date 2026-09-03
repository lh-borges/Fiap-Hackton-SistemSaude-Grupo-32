package br.com.fiap.sus.iam.application.port;

import java.time.Instant;

public record TokenGerado(String token, Instant expiraEm) {
}
