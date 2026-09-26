package br.com.fiap.sus.notificacoes.infrastructure.messaging;

import br.com.fiap.sus.consultas.application.event.ConsultaAgendadaEvent;
import br.com.fiap.sus.consultas.application.event.ConsultaCanceladaEvent;
import br.com.fiap.sus.consultas.application.event.ConsultaRemarcadaEvent;
import br.com.fiap.sus.documentos.application.event.DocumentoEmitidoEvent;
import br.com.fiap.sus.exames.application.event.ExameAgendadoEvent;
import br.com.fiap.sus.exames.application.event.ExameSolicitadoEvent;
import br.com.fiap.sus.notificacoes.application.dto.Destinatario;
import br.com.fiap.sus.notificacoes.application.dto.FatoNotificavel;
import br.com.fiap.sus.notificacoes.application.mensagem.CatalogoMensagens;
import br.com.fiap.sus.notificacoes.application.usecase.ProcessarFatoUseCase;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.pareceres.application.event.ParecerCriadoEvent;
import br.com.fiap.sus.receitas.application.event.ReceitaEmitidaEvent;
import br.com.fiap.sus.receitas.application.event.ReceitaRenovadaEvent;
import br.com.fiap.sus.resultados.application.event.ResultadoExameDisponivelEvent;
import br.com.fiap.sus.shared.domain.event.EventoDominio;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumidor dos eventos do catalogo (specs/000-plataforma-sus/events.md).
 *
 * <p>Cada metodo roda depois do commit da transacao de origem, de forma assincrona e em
 * transacao propria ({@code @ApplicationModuleListener}). O registro de publicacao do
 * Modulith garante reentrega se o processamento falhar (EX-05); a deduplicacao por
 * {@code eventoId} garante que a reentrega nao duplica notificacao (HU-04). E o mesmo
 * caminho com ou sem Kafka (HU-05): o broker recebe apenas a externalizacao.
 */
@Component
public class NotificacaoEventListener {

    private final ProcessarFatoUseCase processar;
    private final DestinatarioResolver destinatarios;

    public NotificacaoEventListener(ProcessarFatoUseCase processar, DestinatarioResolver destinatarios) {
        this.processar = processar;
        this.destinatarios = destinatarios;
    }

    @ApplicationModuleListener
    public void on(ConsultaAgendadaEvent e) {
        processar.executar(fato(e, TipoNotificacao.CONSULTA_AGENDADA, e.consultaId(),
                List.of(destinatarios.paciente(e.pacienteId()), destinatarios.medico(e.medicoId())),
                Map.of(CatalogoMensagens.DATA_HORA, e.dataHora())));
    }

    @ApplicationModuleListener
    public void on(ConsultaRemarcadaEvent e) {
        processar.executar(fato(e, TipoNotificacao.CONSULTA_REMARCADA, e.consultaId(),
                List.of(destinatarios.paciente(e.pacienteId()), destinatarios.medico(e.medicoId())),
                Map.of(CatalogoMensagens.DATA_HORA_ANTERIOR, e.dataHoraAnterior(),
                        CatalogoMensagens.DATA_HORA_NOVA, e.dataHoraNova())));
    }

    @ApplicationModuleListener
    public void on(ConsultaCanceladaEvent e) {
        processar.executar(fato(e, TipoNotificacao.CONSULTA_CANCELADA, e.consultaId(),
                List.of(destinatarios.paciente(e.pacienteId()), destinatarios.medico(e.medicoId())),
                Map.of(CatalogoMensagens.DATA_HORA, e.dataHora())));
    }

    @ApplicationModuleListener
    public void on(ExameSolicitadoEvent e) {
        processar.executar(fato(e, TipoNotificacao.EXAME_SOLICITADO, e.solicitacaoId(),
                List.of(destinatarios.paciente(e.pacienteId())), Map.of()));
    }

    @ApplicationModuleListener
    public void on(ExameAgendadoEvent e) {
        processar.executar(fato(e, TipoNotificacao.EXAME_AGENDADO, e.exameId(),
                List.of(destinatarios.paciente(e.pacienteId())),
                Map.of(CatalogoMensagens.DATA_AGENDADA, e.dataAgendada())));
    }

    @ApplicationModuleListener
    public void on(ResultadoExameDisponivelEvent e) {
        processar.executar(fato(e, TipoNotificacao.RESULTADO_DISPONIVEL, e.resultadoId(),
                List.of(destinatarios.paciente(e.pacienteId()), destinatarios.medico(e.medicoSolicitanteId())),
                Map.of()));
    }

    @ApplicationModuleListener
    public void on(ParecerCriadoEvent e) {
        processar.executar(fato(e, TipoNotificacao.PARECER_CRIADO, e.parecerId(),
                List.of(destinatarios.paciente(e.pacienteId())), Map.of()));
    }

    @ApplicationModuleListener
    public void on(ReceitaEmitidaEvent e) {
        processar.executar(fato(e, TipoNotificacao.RECEITA_EMITIDA, e.receitaId(),
                List.of(destinatarios.paciente(e.pacienteId())),
                parametros(CatalogoMensagens.VALIDADE, e.validade(),
                        CatalogoMensagens.QUANTIDADE_ITENS, e.quantidadeItens())));
    }

    @ApplicationModuleListener
    public void on(ReceitaRenovadaEvent e) {
        processar.executar(fato(e, TipoNotificacao.RECEITA_RENOVADA, e.receitaId(),
                List.of(destinatarios.paciente(e.pacienteId())), Map.of()));
    }

    @ApplicationModuleListener
    public void on(DocumentoEmitidoEvent e) {
        processar.executar(fato(e, TipoNotificacao.DOCUMENTO_EMITIDO, e.documentoId(),
                List.of(destinatarios.paciente(e.pacienteId())), Map.of()));
    }

    private static FatoNotificavel fato(EventoDominio evento, TipoNotificacao tipo, UUID referenciaId,
                                        List<Destinatario> destinatarios, Map<String, Object> parametros) {
        return new FatoNotificavel(evento.eventoId(), evento.getClass().getSimpleName(), tipo, referenciaId,
                destinatarios, parametros);
    }

    /** Map.of nao aceita valor nulo; parametros ausentes sao simplesmente omitidos. */
    private static Map<String, Object> parametros(Object... chaveValor) {
        var mapa = new java.util.HashMap<String, Object>();
        for (int i = 0; i + 1 < chaveValor.length; i += 2) {
            if (chaveValor[i + 1] != null) {
                mapa.put((String) chaveValor[i], chaveValor[i + 1]);
            }
        }
        return mapa;
    }
}
