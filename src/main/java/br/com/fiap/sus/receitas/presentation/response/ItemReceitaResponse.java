package br.com.fiap.sus.receitas.presentation.response;

import br.com.fiap.sus.receitas.application.dto.ItemReceitaOutput;

public record ItemReceitaResponse(String medicamento, String dosagem, String frequencia, String duracao,
                                  String orientacao) {

    public static ItemReceitaResponse de(ItemReceitaOutput output) {
        return new ItemReceitaResponse(output.medicamento(), output.dosagem(), output.frequencia(),
                output.duracao(), output.orientacao());
    }
}