package br.com.fiap.sus.notificacoes.infrastructure.persistence.repository;

import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.mapper.NotificacaoPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class NotificacaoRepositoryAdapter implements NotificacaoRepository {

    private final NotificacaoJpaRepository repository;
    private final NotificacaoPersistenceMapper mapper;

    public NotificacaoRepositoryAdapter(NotificacaoJpaRepository repository, NotificacaoPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Notificacao salvar(Notificacao notificacao) {
        NotificacaoEntity entity = repository.findById(notificacao.getId())
                .map(existente -> {
                    if (notificacao.isLida() && !existente.isLida()) {
                        existente.registrarLeitura(notificacao.getDataLeitura(), notificacao.getAtualizadoEm());
                    }
                    return existente;
                })
                .orElseGet(() -> mapper.paraEntidade(notificacao));
        return mapper.paraDominio(repository.save(entity));
    }

    @Override
    public Optional<Notificacao> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public PaginaResultado<Notificacao> listar(NotificacaoFiltro filtro, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Pagina deve ser positiva ou zero e tamanho entre 1 e 100.");
        }
        var ordem = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending().and(Sort.by("id")));
        Page<NotificacaoEntity> page = filtro.lida() == null
                ? repository.findByUsuarioId(filtro.usuarioId(), ordem)
                : repository.findByUsuarioIdAndLida(filtro.usuarioId(), filtro.lida(), ordem);
        return PaginaResultado.de(page.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, page.getTotalElements());
    }

    @Override
    public long contarNaoLidas(UUID usuarioId) {
        return repository.countByUsuarioIdAndLidaFalse(usuarioId);
    }
}
