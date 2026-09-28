package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.NotificacaoOutput;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-02/RF-03: lista as notificacoes do usuario do token, da mais recente para a mais antiga. */
@Component
public class ListarMinhasNotificacoesUseCase {

    private final NotificacaoRepository notificacoes;
    private final UsuarioAutenticadoProvider usuarios;

    public ListarMinhasNotificacoesUseCase(NotificacaoRepository notificacoes, UsuarioAutenticadoProvider usuarios) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
    }

    @PreAuthorize("isAuthenticated()")
    public PaginaResultado<NotificacaoOutput> executar(Boolean lida, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Pagina deve ser positiva ou zero e tamanho entre 1 e 100.");
        }
        var usuario = usuarios.obrigatorio();
        return notificacoes.listar(new NotificacaoFiltro(usuario.id(), lida), pagina, tamanho)
                .mapear(NotificacaoOutput::de);
    }
}
