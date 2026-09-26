package br.com.fiap.sus.notificacoes.domain.repository;

import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.shared.domain.PaginaResultado;

import java.util.Optional;
import java.util.UUID;

/** Porta de persistencia da notificacao (Artigo II.4). Implementada em infrastructure. */
public interface NotificacaoRepository {

    Notificacao salvar(Notificacao notificacao);

    Optional<Notificacao> buscarPorId(UUID id);

    /** RF-03: da mais recente para a mais antiga, com filtro opcional por situacao de leitura. */
    PaginaResultado<Notificacao> listar(NotificacaoFiltro filtro, int pagina, int tamanho);

    /** RF-05: contagem barata de nao lidas do usuario. */
    long contarNaoLidas(UUID usuarioId);
}
