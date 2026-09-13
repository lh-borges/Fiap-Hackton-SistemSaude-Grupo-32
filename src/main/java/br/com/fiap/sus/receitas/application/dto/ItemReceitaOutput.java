package br.com.fiap.sus.receitas.application.dto;

import br.com.fiap.sus.receitas.domain.model.ItemReceita;

public record ItemReceitaOutput(String medicamento, String dosagem, String frequencia, String duracao,
                                String orientacao) {

    public static ItemReceitaOutput de(ItemReceita item) {
        return new ItemReceitaOutput(item.getMedicamento(), item.getDosagem(), item.getFrequencia(),
                item.getDuracao(), item.getOrientacao());
    }
}