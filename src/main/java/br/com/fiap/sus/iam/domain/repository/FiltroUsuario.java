package br.com.fiap.sus.iam.domain.repository;

import br.com.fiap.sus.iam.domain.enums.RoleNome;

/** Criterios de busca de usuario. Campos nulos nao filtram. */
public record FiltroUsuario(String termo, RoleNome role, Boolean ativo) {

    public static FiltroUsuario vazio() {
        return new FiltroUsuario(null, null, null);
    }
}
