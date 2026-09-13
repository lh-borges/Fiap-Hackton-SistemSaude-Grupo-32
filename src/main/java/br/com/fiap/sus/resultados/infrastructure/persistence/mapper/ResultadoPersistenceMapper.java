package br.com.fiap.sus.resultados.infrastructure.persistence.mapper;

import br.com.fiap.sus.resultados.domain.model.ItemResultadoLaboratorial;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ItemResultadoLaboratorialEntity;
import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ResultadoExameEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ResultadoPersistenceMapper {

    public ResultadoExame paraDominio(ResultadoExameEntity e, List<ItemResultadoLaboratorialEntity> itensEntity) {
        List<ItemResultadoLaboratorial> itens = itensEntity.stream().map(this::paraDominio).toList();
        return ResultadoExame.reconstituir(e.getId(), e.getExameId(), e.getPacienteId(), e.getTipoResultado(),
                e.getDataResultado(), e.getObservacao(), e.getArquivoUrl(), e.getDescricao(), e.getLaudo(),
                itens);
    }

    public ResultadoExameEntity paraEntidade(ResultadoExame r) {
        return new ResultadoExameEntity(r.getId(), r.getExameId(), r.getPacienteId(), r.getTipoResultado(),
                r.getDataResultado(), r.getObservacao(), r.getArquivoUrl(), r.getDescricao(), r.getLaudo());
    }

    public ItemResultadoLaboratorial paraDominio(ItemResultadoLaboratorialEntity e) {
        return ItemResultadoLaboratorial.reconstituir(e.getId(), e.getNomeParametro(), e.getValor(),
                e.getUnidade(), e.getValorMinimoReferencia(), e.getValorMaximoReferencia(),
                e.getResultadoTexto(), e.getSituacao());
    }

    public ItemResultadoLaboratorialEntity paraEntidade(UUID resultadoExameId, ItemResultadoLaboratorial item) {
        return new ItemResultadoLaboratorialEntity(item.getId(), resultadoExameId, item.getNomeParametro(),
                item.getValor(), item.getUnidade(), item.getValorMinimoReferencia(),
                item.getValorMaximoReferencia(), item.getResultadoTexto(), item.getSituacao());
    }
}