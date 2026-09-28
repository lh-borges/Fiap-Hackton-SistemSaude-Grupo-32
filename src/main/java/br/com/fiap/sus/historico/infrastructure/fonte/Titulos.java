package br.com.fiap.sus.historico.infrastructure.fonte;

import java.util.Locale;

/** Titulos dos itens do historico: nomeiam o ato e a situacao, nunca o conteudo clinico (RN-06). */
final class Titulos {
    private Titulos() {
    }

    /** "Consulta" + "AGENDADA" -> "Consulta agendada". */
    static String comSituacao(String ato, String situacao) {
        return ato + " " + situacao.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    /** "Documento" + "ATESTADO" -> "Documento: atestado". */
    static String comQualificador(String ato, String qualificador) {
        return ato + ": " + qualificador.toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
