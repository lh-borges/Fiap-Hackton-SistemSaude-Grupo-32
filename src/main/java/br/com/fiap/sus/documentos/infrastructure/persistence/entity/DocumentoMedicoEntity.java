package br.com.fiap.sus.documentos.infrastructure.persistence.entity;

import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doc_documento_medico")
public class DocumentoMedicoEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipo;

    @Column(nullable = false, length = 10000)
    private String conteudo;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "consulta_id")
    private UUID consultaId;

    @Column(name = "exame_id")
    private UUID exameId;

    @Column(name = "data_emissao", nullable = false)
    private Instant dataEmissao;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoDocumento situacao;

    @Column(name = "motivo_cancelamento", length = 1000)
    private String motivoCancelamento;

    protected DocumentoMedicoEntity() {
    }

    public DocumentoMedicoEntity(
            UUID id,
            TipoDocumento tipo,
            String conteudo,
            UUID pacienteId,
            UUID medicoId,
            UUID consultaId,
            UUID exameId,
            Instant dataEmissao,
            String arquivoUrl,
            SituacaoDocumento situacao,
            String motivoCancelamento
    ) {
        this.id = id;
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.consultaId = consultaId;
        this.exameId = exameId;
        this.dataEmissao = dataEmissao;
        this.arquivoUrl = arquivoUrl;
        this.situacao = situacao;
        this.motivoCancelamento = motivoCancelamento;
    }

    public UUID getId() { return id; }
    public TipoDocumento getTipo() { return tipo; }
    public String getConteudo() { return conteudo; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getConsultaId() { return consultaId; }
    public UUID getExameId() { return exameId; }
    public Instant getDataEmissao() { return dataEmissao; }
    public String getArquivoUrl() { return arquivoUrl; }
    public SituacaoDocumento getSituacao() { return situacao; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
}

