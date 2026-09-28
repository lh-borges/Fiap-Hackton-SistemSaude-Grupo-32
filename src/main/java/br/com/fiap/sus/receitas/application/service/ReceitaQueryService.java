package br.com.fiap.sus.receitas.application.service;

import br.com.fiap.sus.receitas.api.ReceitaQuery;
import br.com.fiap.sus.receitas.api.ReceitaResumo;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceitaQueryService implements ReceitaQuery {

    private final ReceitaRepository repository;

    public ReceitaQueryService(ReceitaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceitaResumo> receitasDoPaciente(UUID pacienteId) {
        return repository.listarPorPaciente(pacienteId).stream().map(ReceitaQueryService::resumo).toList();
    }

    private static ReceitaResumo resumo(Receita r) {
        return new ReceitaResumo(r.getId(), r.getPacienteId(), r.getMedicoId(), r.getConsultaId(),
                r.getDataEmissao(), r.getValidade(), r.getSituacao().name(), r.getReceitaOrigemId());
    }
}
