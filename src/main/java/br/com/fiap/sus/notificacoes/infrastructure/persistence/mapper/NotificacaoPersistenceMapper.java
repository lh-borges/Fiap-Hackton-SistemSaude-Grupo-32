package br.com.fiap.sus.notificacoes.infrastructure.persistence.mapper;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.infrastructure.persistence.entity.NotificacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoPersistenceMapper {

    public NotificacaoEntity paraEntidade(Notificacao n) {
        return new NotificacaoEntity(n.getId(), n.getUsuarioId(), n.getEventoId(), n.getTipo().name(), n.getTitulo(),
                n.getMensagem(), n.getReferenciaId(), n.isLida(), n.getDataLeitura(), n.getCriadoEm(),
                n.getAtualizadoEm());
    }

    public Notificacao paraDominio(NotificacaoEntity e) {
        return Notificacao.reconstituir(e.getId(), e.getUsuarioId(), e.getEventoId(), TipoNotificacao.valueOf(e.getTipo()),
                e.getTitulo(), e.getMensagem(), e.getReferenciaId(), e.isLida(), e.getDataLeitura(), e.getCriadoEm(),
                e.getAtualizadoEm());
    }
}
