package br.com.fiap.sus.receitas.infrastructure.persistence.mapper;

import br.com.fiap.sus.receitas.domain.model.ItemReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ItemReceitaEntity;
import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ReceitaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReceitaPersistenceMapper {

    public Receita paraDominio(ReceitaEntity e, List<ItemReceitaEntity> itensEntity) {
        List<ItemReceita> itens = itensEntity.stream().map(this::paraDominio).toList();
        return Receita.reconstituir(e.getId(), e.getPacienteId(), e.getMedicoId(), e.getConsultaId(), itens,
                e.getDataEmissao(), e.getValidade(), e.getObservacao(), e.getSituacao(),
                e.getReceitaOrigemId(), e.getMotivoCancelamento());
    }

    public ReceitaEntity paraEntidade(Receita r) {
        return new ReceitaEntity(r.getId(), r.getPacienteId(), r.getMedicoId(), r.getConsultaId(),
                r.getDataEmissao(), r.getValidade(), r.getObservacao(), r.getSituacao(),
                r.getReceitaOrigemId(), r.getMotivoCancelamento());
    }

    public ItemReceita paraDominio(ItemReceitaEntity e) {
        return ItemReceita.reconstituir(e.getId(), e.getMedicamento(), e.getDosagem(), e.getFrequencia(),
                e.getDuracao(), e.getOrientacao());
    }

    public ItemReceitaEntity paraEntidade(UUID receitaId, ItemReceita item) {
        return new ItemReceitaEntity(item.getId(), receitaId, item.getMedicamento(), item.getDosagem(),
                item.getFrequencia(), item.getDuracao(), item.getOrientacao());
    }
}