package br.com.fiap.sus.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Identidade do usuario da requisicao atual, extraida do token.
 * E o principal do Spring Security e o objeto que os casos de uso recebem para
 * aplicar a verificacao de posse do dado (Artigo IV.4).
 */
public record UsuarioAutenticado(UUID id, String nome, Set<String> roles) {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ATENDENTE = "ATENDENTE";
    public static final String MEDICO = "MEDICO";
    public static final String PACIENTE = "PACIENTE";

    public UsuarioAutenticado {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean temRole(String role) {
        return roles.contains(role);
    }

    public boolean ehAdministrador() {
        return temRole(ADMINISTRADOR);
    }

    public boolean ehPaciente() {
        return temRole(PACIENTE);
    }

    public boolean ehMedico() {
        return temRole(MEDICO);
    }

    public boolean ehOMesmo(UUID usuarioId) {
        return id.equals(usuarioId);
    }
}
