package br.com.fiap.sus.resultados.presentation.request;

public record ItemResultadoLaboratorialRequest(String nomeParametro, Double valor, String unidade,
                                               Double valorMinimoReferencia, Double valorMaximoReferencia,
                                               String resultadoTexto, String situacao) {
}