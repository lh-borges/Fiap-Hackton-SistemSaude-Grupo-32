package br.com.fiap.sus.resultados.application.dto;

/**
 * valor/unidade preenchidos para parametro quantitativo (situacao sera calculada);
 * resultadoTexto/situacao preenchidos para parametro qualitativo.
 */
public record ItemResultadoLaboratorialDTO(String nomeParametro, Double valor, String unidade,
                                           Double valorMinimoReferencia, Double valorMaximoReferencia,
                                           String resultadoTexto, String situacao) {
}