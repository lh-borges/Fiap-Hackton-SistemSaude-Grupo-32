package br.com.fiap.sus.iam.application.dto;

import br.com.fiap.sus.iam.domain.model.Usuario;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Saida de caso de uso. Nunca carrega o hash da senha. */
public record UsuarioOutput(UUID id, String nome, String cpf, String email, boolean ativo,
                            Set<String> roles, Instant criadoEm) {

    public static UsuarioOutput de(Usuario usuario) {
        return new UsuarioOutput(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf().formatado(),
                usuario.getEmail().valor(),
                usuario.isAtivo(),
                usuario.getRoles().stream().map(Enum::name).collect(Collectors.toCollection(java.util.TreeSet::new)),
                usuario.getCriadoEm());
    }
}
