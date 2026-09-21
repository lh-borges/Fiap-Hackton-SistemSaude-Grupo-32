package br.com.fiap.sus.documentos.domain.enums;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;

import java.util.Arrays;

public enum TipoDocumento {
    ATESTADO(
            "Atestado Medico",
            "Informações do atestado",
            "Atestado"
    ),
    LAUDO(
            "Laudo Médico",
            "Informações do laudo",
            "Laudo e conclusão clínica"
    ),
    RELATORIO(
            "Relatório Médico",
            "Informações do relatório",
            "Relatório clínico"
    ),
    ENCAMINHAMENTO(
            "Encaminhamento Médico",
            "Informações do encaminhamento",
            "Justificativa e destino do encaminhamento"
    ),
    DECLARACAO(
            "Declaração de Comparecimento",
            "Informações da declaração",
            "Declaração de Comparecimento"
    );

    private final String tituloPdf;
    private final String secaoPdf;
    private final String rotuloConteudoPdf;

    TipoDocumento(String tituloPdf, String secaoPdf, String rotuloConteudoPdf) {
        this.tituloPdf = tituloPdf;
        this.secaoPdf = secaoPdf;
        this.rotuloConteudoPdf = rotuloConteudoPdf;
    }

    public String tituloPdf() {
        return tituloPdf;
    }

    public String secaoPdf() {
        return secaoPdf;
    }

    public String rotuloConteudoPdf() {
        return rotuloConteudoPdf;
    }

    public static TipoDocumento de(String valor) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(valor == null ? "" : valor.trim()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Tipo invalido. Valores aceitos: ATESTADO, LAUDO, RELATORIO, ENCAMINHAMENTO, DECLARACAO."));
    }
}
