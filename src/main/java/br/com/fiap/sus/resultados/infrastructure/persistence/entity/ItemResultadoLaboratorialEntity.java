package br.com.fiap.sus.resultados.infrastructure.persistence.entity;

import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "resu_item_resultado_laboratorial")
public class ItemResultadoLaboratorialEntity {

    @Id
    private UUID id;

    @Column(name = "resultado_exame_id", nullable = false)
    private UUID resultadoExameId;

    @Column(name = "nome_parametro", nullable = false, length = 200)
    private String nomeParametro;

    private Double valor;

    @Column(length = 20)
    private String unidade;

    @Column(name = "valor_minimo_referencia")
    private Double valorMinimoReferencia;

    @Column(name = "valor_maximo_referencia")
    private Double valorMaximoReferencia;

    @Column(name = "resultado_texto", length = 200)
    private String resultadoTexto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoParametro situacao;

    protected ItemResultadoLaboratorialEntity() {
    }

    public ItemResultadoLaboratorialEntity(UUID id, UUID resultadoExameId, String nomeParametro, Double valor,
                                           String unidade, Double valorMinimoReferencia,
                                           Double valorMaximoReferencia, String resultadoTexto,
                                           SituacaoParametro situacao) {
        this.id = id;
        this.resultadoExameId = resultadoExameId;
        this.nomeParametro = nomeParametro;
        this.valor = valor;
        this.unidade = unidade;
        this.valorMinimoReferencia = valorMinimoReferencia;
        this.valorMaximoReferencia = valorMaximoReferencia;
        this.resultadoTexto = resultadoTexto;
        this.situacao = situacao;
    }

    public UUID getId() { return id; }
    public UUID getResultadoExameId() { return resultadoExameId; }
    public String getNomeParametro() { return nomeParametro; }
    public Double getValor() { return valor; }
    public String getUnidade() { return unidade; }
    public Double getValorMinimoReferencia() { return valorMinimoReferencia; }
    public Double getValorMaximoReferencia() { return valorMaximoReferencia; }
    public String getResultadoTexto() { return resultadoTexto; }
    public SituacaoParametro getSituacao() { return situacao; }
}