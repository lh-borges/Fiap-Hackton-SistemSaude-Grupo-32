package br.com.fiap.sus.pareceres.application.service;

import br.com.fiap.sus.pareceres.api.ParecerQuery;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParecerQueryService implements ParecerQuery, br.com.fiap.sus.resultados.api.IndicadorParecerQuery {

    private final ParecerMedicoRepository parecerMedicoRepository;

    public ParecerQueryService(ParecerMedicoRepository parecerMedicoRepository) {
        this.parecerMedicoRepository = parecerMedicoRepository;
    }

    @Override
    public java.util.Set<UUID> resultadosComParecer(java.util.Set<UUID> ids) {
        return ids.isEmpty() ? java.util.Set.of() : parecerMedicoRepository.resultadosComParecer(ids);
    }

    @Override
    public boolean existeParecerParaResultado(UUID resultadoExameId) {
        return parecerMedicoRepository.existePorResultadoExameId(resultadoExameId);
    }
}
