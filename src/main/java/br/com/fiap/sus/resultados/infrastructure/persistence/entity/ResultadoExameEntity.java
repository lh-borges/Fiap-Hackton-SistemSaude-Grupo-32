package br.com.fiap.sus.resultados.infrastructure.persistence.entity;

import br.com.fiap.sus.resultados.domain.enums.TipoResultado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resu_resultado_exame")
public class ResultadoExameEntity {

    @Id
    private UUID id;

    @Column(name = "exame_id", nullable = false, unique = true)
    private UUID exameId;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_resultado", nullable = false, length = 20)
    private TipoResultado tipoResultado;

    @Column(name = "data_resultado", nullable = false)
    private Instant dataResultado;

    @Column(length = 2000)
    private String observacao;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(length = 2000)
    private String descricao;

    @Column(length = 5000)
    private String laudo;

    protected ResultadoExameEntity() {
    }

    public ResultadoExameEntity(UUID id, UUID exameId, UUID pacienteId, TipoResultado tipoResultado,
                                Instant dataResultado, String observacao, String arquivoUrl,
                                String descricao, String laudo) {
        this.id = id;
        this.exameId = exameId;
        this.pacienteId = pacienteId;
        this.tipoResultado = tipoResultado;
        this.dataResultado = dataResultado;
        this.observacao = observacao;
        this.arquivoUrl = arquivoUrl;
        this.descricao = descricao;
        this.laudo = laudo;
    }

    public UUID getId() { return id; }
    public UUID getExameId() { return exameId; }
    public UUID getPacienteId() { return pacienteId; }
    public TipoResultado getTipoResultado() { return tipoResultado; }
    public Instant getDataResultado() { return dataResultado; }
    public String getObservacao() { return observacao; }
    public String getArquivoUrl() { return arquivoUrl; }
    public String getDescricao() { return descricao; }
    public String getLaudo() { return laudo; }
}