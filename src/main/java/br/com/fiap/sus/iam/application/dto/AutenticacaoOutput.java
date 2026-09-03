package br.com.fiap.sus.iam.application.dto;

import java.time.Instant;

public record AutenticacaoOutput(String token, String tipo, Instant expiraEm, UsuarioOutput usuario) {

    public static AutenticacaoOutput de(String token, Instant expiraEm, UsuarioOutput usuario) {
        return new AutenticacaoOutput(token, "Bearer", expiraEm, usuario);
    }
}
