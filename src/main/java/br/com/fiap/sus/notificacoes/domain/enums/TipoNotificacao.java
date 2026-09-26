package br.com.fiap.sus.notificacoes.domain.enums;

/**
 * Um valor por fato de RF-08. Cada tipo conhece o recurso da API onde o registro de origem
 * ({@code referenciaId}) pode ser consultado, para a navegacao da HU-06.
 */
public enum TipoNotificacao {

    CONSULTA_AGENDADA("consultas"),
    CONSULTA_REMARCADA("consultas"),
    CONSULTA_CANCELADA("consultas"),
    EXAME_SOLICITADO("solicitacoes-exame"),
    EXAME_AGENDADO("exames"),
    RESULTADO_DISPONIVEL("resultados-exame"),
    PARECER_CRIADO("pareceres"),
    RECEITA_EMITIDA("receitas"),
    RECEITA_RENOVADA("receitas"),
    DOCUMENTO_EMITIDO("documentos");

    private final String recurso;

    TipoNotificacao(String recurso) {
        this.recurso = recurso;
    }

    /** Segmento de recurso em {@code /api/v1/<recurso>/{referenciaId}}. */
    public String getRecurso() {
        return recurso;
    }
}
