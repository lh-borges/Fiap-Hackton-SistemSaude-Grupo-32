package br.com.fiap.sus.resultados.application.service;

import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.resultados.api.ResultadoResumo;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultadoQueryService implements ResultadoQuery {

    private final ResultadoExameRepository repository;

    public ResultadoQueryService(ResultadoExameRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> pacienteIdDoResultado(UUID resultadoId) {
        return repository.buscarPorId(resultadoId).map(ResultadoExame::getPacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultadoResumo> resultadoDoExame(UUID exameId) {
        return repository.buscarPorExameId(exameId).map(this::resumo);
    }

    private ResultadoResumo resumo(ResultadoExame resultado) {
        return new ResultadoResumo(
                resultado.getId(),
                resultado.getExameId(),
                resultado.getPacienteId(),
                resultado.getTipoResultado().name(),
                resultado.getDataResultado(),
                resultado.getObservacao(),
                resultado.getArquivoUrl(),
                resultado.getDescricao(),
                resultado.getLaudo()
        );
    }
}
