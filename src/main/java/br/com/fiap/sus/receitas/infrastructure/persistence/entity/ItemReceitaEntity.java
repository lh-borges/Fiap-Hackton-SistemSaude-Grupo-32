package br.com.fiap.sus.receitas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rece_item_receita")
public class ItemReceitaEntity {

    @Id
    private UUID id;

    @Column(name = "receita_id", nullable = false)
    private UUID receitaId;

    @Column(nullable = false, length = 200)
    private String medicamento;

    @Column(nullable = false, length = 100)
    private String dosagem;

    @Column(nullable = false, length = 100)
    private String frequencia;

    @Column(nullable = false, length = 100)
    private String duracao;

    @Column(length = 500)
    private String orientacao;

    protected ItemReceitaEntity() {
    }

    public ItemReceitaEntity(UUID id, UUID receitaId, String medicamento, String dosagem, String frequencia,
                             String duracao, String orientacao) {
        this.id = id;
        this.receitaId = receitaId;
        this.medicamento = medicamento;
        this.dosagem = dosagem;
        this.frequencia = frequencia;
        this.duracao = duracao;
        this.orientacao = orientacao;
    }

    public UUID getId() { return id; }
    public UUID getReceitaId() { return receitaId; }
    public String getMedicamento() { return medicamento; }
    public String getDosagem() { return dosagem; }
    public String getFrequencia() { return frequencia; }
    public String getDuracao() { return duracao; }
    public String getOrientacao() { return orientacao; }
}