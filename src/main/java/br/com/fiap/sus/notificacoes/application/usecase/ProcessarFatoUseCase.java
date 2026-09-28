package br.com.fiap.sus.notificacoes.application.usecase;

import br.com.fiap.sus.notificacoes.application.dto.Destinatario;
import br.com.fiap.sus.notificacoes.application.dto.FatoNotificavel;
import br.com.fiap.sus.notificacoes.application.mensagem.CatalogoMensagens;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.EventoProcessadoRepository;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * RF-01/RF-06: transforma um fato do atendimento em notificacoes, uma por destinatario
 * (RN-02), sem nunca repetir o mesmo fato (RN-03, HU-04).
 *
 * <p>Nao e exposto por HTTP: so o listener de eventos o chama. Roda na transacao aberta
 * pelo listener; o marcador de processamento e as notificacoes sao gravados juntos, de
 * modo que uma falha no meio deixa tudo para a proxima entrega (EX-05).
 *
 * <p>Logs registram apenas identificadores opacos (Artigo IV.6).
 */
@Component
public class ProcessarFatoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessarFatoUseCase.class);

    private final NotificacaoRepository notificacoes;
    private final EventoProcessadoRepository eventosProcessados;
    private final CatalogoMensagens catalogo;

    public ProcessarFatoUseCase(NotificacaoRepository notificacoes, EventoProcessadoRepository eventosProcessados,
                                CatalogoMensagens catalogo) {
        this.notificacoes = notificacoes;
        this.eventosProcessados = eventosProcessados;
        this.catalogo = catalogo;
    }

    /** @return quantidade de notificacoes criadas (zero para fato duplicado). */
    @Transactional
    public int executar(FatoNotificavel fato) {
        if (eventosProcessados.jaProcessado(fato.eventoId())) {
            log.info("Fato {} ({}) ja processado; descartado.", fato.eventoId(), fato.tipoEvento());
            return 0;
        }
        eventosProcessados.registrar(fato.eventoId(), fato.tipoEvento(), Instant.now());

        int criadas = 0;
        for (Destinatario destinatario : fato.destinatarios()) {
            if (!destinatario.resolvido()) {
                log.warn("Fato {} ({}): destinatario {} sem usuario vinculado; descartado.",
                        fato.eventoId(), fato.tipoEvento(), destinatario.papel());
                continue;
            }
            var mensagem = catalogo.montar(fato.tipo(), destinatario.papel(), fato.parametros());
            notificacoes.salvar(Notificacao.criar(destinatario.usuarioId(), fato.eventoId(), fato.tipo(),
                    mensagem.titulo(), mensagem.texto(), fato.referenciaId()));
            criadas++;
        }
        log.info("Fato {} ({}) gerou {} notificacao(oes).", fato.eventoId(), fato.tipoEvento(), criadas);
        return criadas;
    }
}
