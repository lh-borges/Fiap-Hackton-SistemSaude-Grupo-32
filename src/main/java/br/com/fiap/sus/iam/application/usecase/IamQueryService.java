package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.FiltroUsuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementacao da porta publicada em iam.api. */
@Service
public class IamQueryService implements IamQuery {

    private final UsuarioRepository usuarios;

    public IamQueryService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResumo> resumoDoUsuario(UUID usuarioId) {
        return usuarios.porId(usuarioId).map(this::converter);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usuarioAtivoExiste(UUID usuarioId) {
        return usuarios.porId(usuarioId).map(Usuario::isAtivo).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usuarioAtivoPossuiRole(UUID usuarioId, String role) {
        return usuarios.porId(usuarioId)
                .filter(Usuario::isAtivo)
                .map(usuario -> usuario.temRole(RoleNome.de(role)))
                .orElse(false);
    }

    private UsuarioResumo converter(Usuario usuario) {
        return new UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail().valor(),
                usuario.isAtivo(), usuario.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.UUID> idsDeUsuariosPorTermo(String termo, int limite) {
        if (termo == null || termo.isBlank()) {
            return java.util.List.of();
        }
        int tamanho = Math.min(Math.max(limite, 1), 500);
        return usuarios.buscar(new FiltroUsuario(termo.trim(), null, null), 0, tamanho)
                .conteudo().stream().map(Usuario::getId).toList();
    }
}
