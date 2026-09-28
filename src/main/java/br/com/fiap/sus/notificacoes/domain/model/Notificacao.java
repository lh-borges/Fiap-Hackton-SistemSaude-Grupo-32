package br.com.fiap.sus.notificacoes.domain.model;

import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Aviso interno destinado a exatamente um usuario (RN-01). Nasce nao lida e a leitura e
 * irreversivel (RN-04). A mensagem nunca contem dado clinico (RN-05): quem a monta e o
 * catalogo de mensagens da aplicacao, a partir de identificadores e datas.
 */
public final class Notificacao {

    public static final int TITULO_MAX = 150;
    public static final int MENSAGEM_MAX = 500;

    private final UUID id;
    private final UUID usuarioId;
    private final UUID eventoId;
    private final TipoNotificacao tipo;
    private final String titulo;
    private final String mensagem;
    private final UUID referenciaId;
    private boolean lida;
    private Instant dataLeitura;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Notificacao(UUID id, UUID usuarioId, UUID eventoId, TipoNotificacao tipo, String titulo,
                        String mensagem, UUID referenciaId, boolean lida, Instant dataLeitura,
                        Instant criadoEm, Instant atualizadoEm) {
        if (isNull(id)) {
            throw new RegraDeNegocioException("O identificador da notificacao e obrigatorio.");
        }
        if (isNull(usuarioId)) {
            throw new RegraDeNegocioException("A notificacao deve ter um usuario destinatario.");
        }
        if (isNull(eventoId)) {
            throw new RegraDeNegocioException("A notificacao deve referenciar o evento que a originou.");
        }
        if (isNull(tipo)) {
            throw new RegraDeNegocioException("O tipo da notificacao e obrigatorio.");
        }
        if (isNull(criadoEm)) {
            throw new RegraDeNegocioException("A data de criacao da notificacao e obrigatoria.");
        }
        if (lida == isNull(dataLeitura)) {
            throw new RegraDeNegocioException("Notificacao lida exige data de leitura, e nao lida nao pode te-la.");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.titulo = texto(titulo, "titulo", TITULO_MAX);
        this.mensagem = texto(mensagem, "mensagem", MENSAGEM_MAX);
        this.referenciaId = referenciaId;
        this.lida = lida;
        this.dataLeitura = dataLeitura;
        this.criadoEm = criadoEm;
        this.atualizadoEm = isNull(atualizadoEm) ? criadoEm : atualizadoEm;
    }

    private static String texto(String valor, String campo, int maximo) {
        String tratado = isNull(valor) ? "" : valor.trim();
        if (tratado.isEmpty()) {
            throw new RegraDeNegocioException("O campo " + campo + " da notificacao e obrigatorio.");
        }
        if (tratado.length() > maximo) {
            throw new RegraDeNegocioException("O campo " + campo + " da notificacao excede " + maximo + " caracteres.");
        }
        return tratado;
    }

    /**
     * Instante com precisao de microssegundos, a mesma que o banco guarda: assim a resposta
     * montada em memoria e a relida do banco sao identicas.
     */
    public static Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    /** HU-01: toda notificacao nasce nao lida. */
    public static Notificacao criar(UUID usuarioId, UUID eventoId, TipoNotificacao tipo, String titulo,
                                    String mensagem, UUID referenciaId) {
        Instant agora = agora();
        return new Notificacao(UUID.randomUUID(), usuarioId, eventoId, tipo, titulo, mensagem, referenciaId,
                false, null, agora, agora);
    }

    public static Notificacao reconstituir(UUID id, UUID usuarioId, UUID eventoId, TipoNotificacao tipo,
                                           String titulo, String mensagem, UUID referenciaId, boolean lida,
                                           Instant dataLeitura, Instant criadoEm, Instant atualizadoEm) {
        return new Notificacao(id, usuarioId, eventoId, tipo, titulo, mensagem, referenciaId, lida, dataLeitura,
                criadoEm, atualizadoEm);
    }

    /** Regra de posse (HU-03, EX-04): quem nao e o destinatario trata o registro como inexistente. */
    public boolean pertenceA(UUID usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    /**
     * RN-04: a leitura e irreversivel e registrada uma unica vez. Marcar de novo nao altera
     * a data original.
     *
     * @return {@code true} se a notificacao passou de nao lida para lida nesta chamada
     */
    public boolean marcarComoLida(Instant agora) {
        if (lida) {
            return false;
        }
        if (isNull(agora)) {
            throw new RegraDeNegocioException("O instante da leitura e obrigatorio.");
        }
        this.lida = true;
        this.dataLeitura = agora;
        this.atualizadoEm = agora;
        return true;
    }

    public boolean marcarComoLida() {
        return marcarComoLida(agora());
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getEventoId() { return eventoId; }
    public TipoNotificacao getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public UUID getReferenciaId() { return referenciaId; }
    public boolean isLida() { return lida; }
    public Instant getDataLeitura() { return dataLeitura; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
