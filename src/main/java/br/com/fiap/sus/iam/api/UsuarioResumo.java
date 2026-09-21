package br.com.fiap.sus.iam.api;

import java.util.Set;
import java.util.UUID;

/** Visao minima de um usuario, exposta a outros modulos. Sem hash de senha. */
public record UsuarioResumo(UUID id, String nome, String cpf, String email, boolean ativo, Set<String> roles) {
}
