package br.com.fiap.sus.receitas.domain.model;

import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.UUID;

/** Item de uma receita. Imutavel apos a criacao (RN-07 da receita). */
public class ItemReceita {

    private final UUID id;
    private final String medicamento;
    private final String dosagem;
    private final String frequencia;
    private final String duracao;
    private final String orientacao;

    private ItemReceita(UUID id, String medicamento, String dosagem, String frequencia,
                        String duracao, String orientacao) {
        if (medicamento == null || medicamento.trim().length() < 2) {
            throw new RegraDeNegocioException("O medicamento e obrigatorio.");
        }
        if (dosagem == null || dosagem.isBlank()) {
            throw new RegraDeNegocioException("A dosagem e obrigatoria.");
        }
        if (frequencia == null || frequencia.isBlank()) {
            throw new RegraDeNegocioException("A frequencia e obrigatoria.");
        }
        if (duracao == null || duracao.isBlank()) {
            throw new RegraDeNegocioException("A duracao e obrigatoria.");
        }
        this.id = id;
        this.medicamento = medicamento.trim();
        this.dosagem = dosagem.trim();
        this.frequencia = frequencia.trim();
        this.duracao = duracao.trim();
        this.orientacao = orientacao == null || orientacao.isBlank() ? null : orientacao.trim();
    }

    public static ItemReceita criar(String medicamento, String dosagem, String frequencia,
                                    String duracao, String orientacao) {
        return new ItemReceita(UUID.randomUUID(), medicamento, dosagem, frequencia, duracao, orientacao);
    }

    public static ItemReceita reconstituir(UUID id, String medicamento, String dosagem, String frequencia,
                                           String duracao, String orientacao) {
        return new ItemReceita(id, medicamento, dosagem, frequencia, duracao, orientacao);
    }

    public UUID getId() {
        return id;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public String getDuracao() {
        return duracao;
    }

    public String getOrientacao() {
        return orientacao;
    }
}