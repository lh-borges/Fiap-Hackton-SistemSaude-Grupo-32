package br.com.fiap.sus.historico.domain.enums;

/**
 * Tipo do registro de origem de um item da linha do tempo, na ordem natural do atendimento.
 * A ordem dos valores e usada como criterio de desempate (RN-07).
 */
public enum TipoRegistro {
    CONSULTA("consultas"),
    SOLICITACAO_EXAME("solicitacoes-exame"),
    EXAME("exames"),
    RESULTADO_EXAME("resultados-exame"),
    PARECER("pareceres"),
    RECEITA("receitas"),
    DOCUMENTO("documentos");

    private final String recurso;

    TipoRegistro(String recurso) {
        this.recurso = recurso;
    }

    /** Nome do recurso REST (sem o prefixo /api/v1) onde o detalhe do registro e consultado. */
    public String recurso() {
        return recurso;
    }
}
