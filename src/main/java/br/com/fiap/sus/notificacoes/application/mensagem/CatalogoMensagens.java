package br.com.fiap.sus.notificacoes.application.mensagem;

import br.com.fiap.sus.notificacoes.application.dto.PapelDestinatario;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

/**
 * Titulo e mensagem de cada tipo de notificacao, por papel do destinatario.
 *
 * <p>RN-05: as mensagens informam que um registro existe e quando, nunca o seu conteudo.
 * Os unicos parametros interpolados sao datas e contagens; nenhum valor de exame, laudo,
 * diagnostico ou medicamento passa por aqui.
 */
@Component
public class CatalogoMensagens {

    public static final String DATA_HORA = "dataHora";
    public static final String DATA_HORA_ANTERIOR = "dataHoraAnterior";
    public static final String DATA_HORA_NOVA = "dataHoraNova";
    public static final String DATA_AGENDADA = "dataAgendada";
    public static final String VALIDADE = "validade";
    public static final String QUANTIDADE_ITENS = "quantidadeItens";

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(FUSO);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(FUSO);
    private static final String DATA_INDEFINIDA = "data a confirmar";

    public record Mensagem(String titulo, String texto) {
    }

    public Mensagem montar(TipoNotificacao tipo, PapelDestinatario papel, Map<String, Object> parametros) {
        boolean medico = papel == PapelDestinatario.MEDICO;
        return switch (tipo) {
            case CONSULTA_AGENDADA -> medico
                    ? new Mensagem("Nova consulta na sua agenda",
                    "Uma consulta foi agendada na sua agenda para " + dataHora(parametros, DATA_HORA) + ".")
                    : new Mensagem("Consulta agendada",
                    "Sua consulta foi agendada para " + dataHora(parametros, DATA_HORA) + ".");
            case CONSULTA_REMARCADA -> medico
                    ? new Mensagem("Consulta remarcada na sua agenda",
                    "A consulta de " + dataHora(parametros, DATA_HORA_ANTERIOR) + " foi remarcada para "
                            + dataHora(parametros, DATA_HORA_NOVA) + ".")
                    : new Mensagem("Consulta remarcada",
                    "Sua consulta de " + dataHora(parametros, DATA_HORA_ANTERIOR) + " foi remarcada para "
                            + dataHora(parametros, DATA_HORA_NOVA) + ".");
            case CONSULTA_CANCELADA -> medico
                    ? new Mensagem("Consulta cancelada na sua agenda",
                    "A consulta de " + dataHora(parametros, DATA_HORA) + " foi cancelada.")
                    : new Mensagem("Consulta cancelada",
                    "Sua consulta de " + dataHora(parametros, DATA_HORA) + " foi cancelada.");
            case EXAME_SOLICITADO -> new Mensagem("Exame solicitado",
                    "Seu médico solicitou um exame. Procure uma unidade de saúde para agendá-lo.");
            case EXAME_AGENDADO -> new Mensagem("Exame agendado",
                    "Seu exame foi agendado para " + dataHora(parametros, DATA_AGENDADA) + ".");
            case RESULTADO_DISPONIVEL -> medico
                    ? new Mensagem("Resultado de exame disponível",
                    "Um resultado de exame que você solicitou está disponível para análise.")
                    : new Mensagem("Resultado de exame disponível",
                    "O resultado do seu exame já está disponível para consulta.");
            case PARECER_CRIADO -> new Mensagem("Parecer médico disponível",
                    "Seu médico registrou um parecer sobre o resultado do seu exame.");
            case RECEITA_EMITIDA -> new Mensagem("Receita emitida",
                    "Uma nova receita foi emitida para você, válida até " + data(parametros, VALIDADE) + ".");
            case RECEITA_RENOVADA -> new Mensagem("Receita renovada",
                    "Sua receita foi renovada e já está disponível.");
            case DOCUMENTO_EMITIDO -> new Mensagem("Documento médico emitido",
                    "Um novo documento médico está disponível para você.");
        };
    }

    private static String dataHora(Map<String, Object> parametros, String chave) {
        return formatar(parametros.get(chave), FORMATO);
    }

    private static String data(Map<String, Object> parametros, String chave) {
        return formatar(parametros.get(chave), FORMATO_DATA);
    }

    private static String formatar(Object valor, DateTimeFormatter formato) {
        if (valor instanceof Instant instante) {
            return formato.format(instante);
        }
        if (valor instanceof TemporalAccessor temporal) {
            return formato.format(temporal);
        }
        return DATA_INDEFINIDA;
    }
}
