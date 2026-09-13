package br.com.fiap.sus.receitas.domain.repository;

import br.com.fiap.sus.receitas.domain.enums.SituacaoReceita;
import java.util.UUID;

public record ReceitaFiltro(UUID pacienteId, UUID medicoId, SituacaoReceita situacao) {
}