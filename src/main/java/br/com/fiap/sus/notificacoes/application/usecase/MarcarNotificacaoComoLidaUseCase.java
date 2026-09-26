package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.NotificacaoOutput;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** RF-04/RN-04/EX-04: marca como lida uma notificacao propria; de terceiro responde como inexistente. */
@Component
public class MarcarNotificacaoComoLidaUseCase {

    private final NotificacaoRepository notificacoes;
    private final UsuarioAutenticadoProvider usuarios;

    public MarcarNotificacaoComoLidaUseCase(NotificacaoRepository notificacoes, UsuarioAutenticadoProvider usuarios) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public NotificacaoOutput executar(UUID notificacaoId) {
        var usuario = usuarios.obrigatorio();
        Notificacao notificacao = notificacoes.buscarPorId(notificacaoId)
                .filter(n -> n.pertenceA(usuario.id()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificacao nao encontrada."));
        if (notificacao.marcarComoLida()) {
            notificacao = notificacoes.salvar(notificacao);
        }
        return NotificacaoOutput.de(notificacao);
    }
}
