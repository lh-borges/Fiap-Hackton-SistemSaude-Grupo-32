package br.com.fiap.sus.shared.infrastructure.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Acesso ao usuario da requisicao atual sem espalhar o SecurityContextHolder pelo codigo. */
@Component
public class UsuarioAutenticadoProvider {

    public Optional<UsuarioAutenticado> atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || !(autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    public UsuarioAutenticado obrigatorio() {
        return atual().orElseThrow(() -> new IllegalStateException("Nenhum usuario autenticado no contexto."));
    }
}
