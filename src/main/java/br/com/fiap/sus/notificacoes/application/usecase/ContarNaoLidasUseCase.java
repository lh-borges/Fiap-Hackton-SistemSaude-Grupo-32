package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.ContagemNaoLidasOutput;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** RF-05: quantidade de nao lidas do usuario do token. */
@Component
public class ContarNaoLidasUseCase {

    private final NotificacaoRepository notificacoes;
    private final UsuarioAutenticadoProvider usuarios;

    public ContarNaoLidasUseCase(NotificacaoRepository notificacoes, UsuarioAutenticadoProvider usuarios) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
    }

    @PreAuthorize("isAuthenticated()")
    public ContagemNaoLidasOutput executar() {
        var usuario = usuarios.obrigatorio();
        return new ContagemNaoLidasOutput(notificacoes.contarNaoLidas(usuario.id()));
    }
}
