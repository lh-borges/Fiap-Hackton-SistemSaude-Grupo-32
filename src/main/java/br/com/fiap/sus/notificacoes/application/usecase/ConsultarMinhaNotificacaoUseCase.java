package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.NotificacaoOutput;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** HU-06/HU-03: detalhe de uma notificacao propria; a de terceiro e tratada como inexistente. */
@Component
public class ConsultarMinhaNotificacaoUseCase {

    private final NotificacaoRepository notificacoes;
    private final UsuarioAutenticadoProvider usuarios;

    public ConsultarMinhaNotificacaoUseCase(NotificacaoRepository notificacoes, UsuarioAutenticadoProvider usuarios) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
    }

    @PreAuthorize("isAuthenticated()")
    public NotificacaoOutput executar(UUID notificacaoId) {
        var usuario = usuarios.obrigatorio();
        return notificacoes.buscarPorId(notificacaoId)
                .filter(n -> n.pertenceA(usuario.id()))
                .map(NotificacaoOutput::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificacao nao encontrada."));
    }
}
