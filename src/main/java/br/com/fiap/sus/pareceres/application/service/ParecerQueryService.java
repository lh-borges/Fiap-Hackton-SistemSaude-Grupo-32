package br.com.fiap.sus.pareceres.application.service;

import br.com.fiap.sus.pareceres.api.ParecerQuery;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.resultados.api.IndicadorParecerQuery;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ParecerQueryService implements ParecerQuery, IndicadorParecerQuery {

    private final ParecerMedicoRepository parecerMedicoRepository;

    public ParecerQueryService(
            ParecerMedicoRepository parecerMedicoRepository
    ) {
        this.parecerMedicoRepository = parecerMedicoRepository;
    }

    @Override
    public Set<UUID> resultadosComParecer(Set<UUID> ids) {
        return ids.isEmpty() ? Set.of() : parecerMedicoRepository.resultadosComParecer(ids);
    }

    @Override
    public boolean existeParecerParaResultado(UUID resultadoExameId) {
        return parecerMedicoRepository.existePorResultadoExameId(resultadoExameId);
    }
}
