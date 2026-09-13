package br.com.fiap.sus.resultados.domain.model;

import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.UUID;

/**
 * Parametro de um resultado laboratorial (feature 005-resultados).
 * RN-06: valor numerico + unidade, OU resultado em texto; nunca ambos vazios.
 * RN-07: faixa de referencia so se aplica a parametro numerico; minimo <= maximo.
 */
public class ItemResultadoLaboratorial {

    private final UUID id;
    private final String nomeParametro;
    private final Double valor;
    private final String unidade;
    private final Double valorMinimoReferencia;
    private final Double valorMaximoReferencia;
    private final String resultadoTexto;
    private final SituacaoParametro situacao;

    private ItemResultadoLaboratorial(UUID id, String nomeParametro, Double valor, String unidade,
                                      Double valorMinimoReferencia, Double valorMaximoReferencia,
                                      String resultadoTexto, SituacaoParametro situacao) {
        if (nomeParametro == null || nomeParametro.trim().length() < 2) {
            throw new RegraDeNegocioException("O nome do parametro e obrigatorio.");
        }
        boolean temValorNumerico = valor != null && unidade != null && !unidade.isBlank();
        boolean temTexto = resultadoTexto != null && !resultadoTexto.isBlank();
        // RN-06
        if (!temValorNumerico && !temTexto) {
            throw new RegraDeNegocioException(
                    "O parametro '" + nomeParametro + "' precisa de valor numerico com unidade, ou de um resultado em texto.");
        }
        if (temValorNumerico && (valorMinimoReferencia != null && valorMaximoReferencia != null)) {
            // RN-07
            if (valorMinimoReferencia > valorMaximoReferencia) {
                throw new RegraDeNegocioException(
                        "A faixa de referencia do parametro '" + nomeParametro + "' esta invertida.");
            }
        }
        this.id = id;
        this.nomeParametro = nomeParametro.trim();
        this.valor = valor;
        this.unidade = unidade;
        this.valorMinimoReferencia = valorMinimoReferencia;
        this.valorMaximoReferencia = valorMaximoReferencia;
        this.resultadoTexto = temTexto ? resultadoTexto.trim() : null;
        // RF-04: calcula automaticamente quando ha valor numerico e faixa; senao usa o que foi informado.
        this.situacao = temValorNumerico
                ? calcularSituacao(valor, valorMinimoReferencia, valorMaximoReferencia, situacao)
                : exigirSituacaoInformada(situacao, nomeParametro);
    }

    /** Fabrica para parametro quantitativo: a situacao e sempre calculada, nunca recebida. */
    public static ItemResultadoLaboratorial quantitativo(String nomeParametro, double valor, String unidade,
                                                         Double valorMinimoReferencia,
                                                         Double valorMaximoReferencia) {
        return new ItemResultadoLaboratorial(UUID.randomUUID(), nomeParametro, valor, unidade,
                valorMinimoReferencia, valorMaximoReferencia, null, null);
    }

    /** Fabrica para parametro qualitativo: a situacao e informada explicitamente (HU-03). */
    public static ItemResultadoLaboratorial qualitativo(String nomeParametro, String resultadoTexto,
                                                        SituacaoParametro situacao) {
        return new ItemResultadoLaboratorial(UUID.randomUUID(), nomeParametro, null, null, null, null,
                resultadoTexto, situacao);
    }

    public static ItemResultadoLaboratorial reconstituir(UUID id, String nomeParametro, Double valor,
                                                         String unidade, Double valorMinimoReferencia,
                                                         Double valorMaximoReferencia, String resultadoTexto,
                                                         SituacaoParametro situacao) {
        return new ItemResultadoLaboratorial(id, nomeParametro, valor, unidade, valorMinimoReferencia,
                valorMaximoReferencia, resultadoTexto, situacao);
    }

    private static SituacaoParametro calcularSituacao(double valor, Double min, Double max,
                                                      SituacaoParametro situacaoInformada) {
        if (min == null || max == null) {
            // sem faixa cadastrada, nao ha o que calcular; aceita o que foi informado (pode ser null).
            return situacaoInformada;
        }
        if (valor < min) {
            return SituacaoParametro.ABAIXO_REFERENCIA;
        }
        if (valor > max) {
            return SituacaoParametro.ACIMA_REFERENCIA;
        }
        return SituacaoParametro.NORMAL;
    }

    private static SituacaoParametro exigirSituacaoInformada(SituacaoParametro situacao, String nomeParametro) {
        if (situacao == null) {
            throw new RegraDeNegocioException(
                    "A situacao do parametro qualitativo '" + nomeParametro + "' e obrigatoria.");
        }
        return situacao;
    }

    public UUID getId() {
        return id;
    }

    public String getNomeParametro() {
        return nomeParametro;
    }

    public Double getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public Double getValorMinimoReferencia() {
        return valorMinimoReferencia;
    }

    public Double getValorMaximoReferencia() {
        return valorMaximoReferencia;
    }

    public String getResultadoTexto() {
        return resultadoTexto;
    }

    public SituacaoParametro getSituacao() {
        return situacao;
    }
}