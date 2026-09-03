package br.com.fiap.sus.iam.domain.model;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import java.util.UUID;

/** Perfil de autorizacao persistido (tabela ROLE do modelo ER). */
public record Role(UUID id, RoleNome nome) {
}
