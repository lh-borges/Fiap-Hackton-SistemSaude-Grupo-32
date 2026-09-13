package br.com.fiap.sus.resultados.infrastructure.persistence.repository;

import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameFiltro;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ItemResultadoLaboratorialEntity;
import br.com.fiap.sus.resultados.infrastructure.persistence.entity.ResultadoExameEntity;
import br.com.fiap.sus.resultados.infrastructure.persistence.mapper.ResultadoPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ResultadoExameRepositoryAdapter implements ResultadoExameRepository {

    private final ResultadoExameJpaRepository resultadoJpa;
    private final ItemResultadoLaboratorialJpaRepository itemJpa;
    private final ResultadoPersistenceMapper mapper;

    public ResultadoExameRepositoryAdapter(ResultadoExameJpaRepository resultadoJpa,
                                           ItemResultadoLaboratorialJpaRepository itemJpa,
                                           ResultadoPersistenceMapper mapper) {
        this.resultadoJpa = resultadoJpa;
        this.itemJpa = itemJpa;
        this.mapper = mapper;
    }

    /** @Transactional necessario aqui: salvar o resultado e seus itens sao duas
     *  chamadas de repositorio diferentes; sem isso, uma falha na segunda deixaria
     *  um ResultadoExame orfao, sem itens (viola RN-05). */
    @Override
    @Transactional
    public ResultadoExame salvar(ResultadoExame resultadoExame) {
        ResultadoExameEntity entidadeSalva = resultadoJpa.save(mapper.paraEntidade(resultadoExame));

        var itensEntity = resultadoExame.getItens().stream()
                .map(item -> mapper.paraEntidade(entidadeSalva.getId(), item))
                .toList();
        itemJpa.saveAll(itensEntity);

        return mapper.paraDominio(entidadeSalva, itemJpa.findByResultadoExameId(entidadeSalva.getId()));
    }

    @Override
    public Optional<ResultadoExame> buscarPorId(UUID id) {
        return resultadoJpa.findById(id)
                .map(e -> mapper.paraDominio(e, itemJpa.findByResultadoExameId(e.getId())));
    }

    @Override
    public boolean existePorExameId(UUID exameId) {
        return resultadoJpa.existsByExameId(exameId);
    }

    @Override
    public Optional<ResultadoExame> buscarPorExameId(UUID exameId) {
        return resultadoJpa.findByExameId(exameId)
                .map(e -> mapper.paraDominio(e, itemJpa.findByResultadoExameId(e.getId())));
    }

    @Override
    public PaginaResultado<ResultadoExame> listar(ResultadoExameFiltro filtro, int pagina, int tamanho) {
        Specification<ResultadoExameEntity> spec = construirFiltro(filtro);
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("dataResultado").descending());
        Page<ResultadoExameEntity> resultado = resultadoJpa.findAll(spec, paginacao);

        List<UUID> idsDaPagina = resultado.getContent().stream()
                .map(ResultadoExameEntity::getId)
                .toList();

        // 1 query so para todos os itens de todos os resultados da pagina (evita N+1).
        Map<UUID, List<ItemResultadoLaboratorialEntity>> itensPorResultado = itemJpa
                .findByResultadoExameIdIn(idsDaPagina).stream()
                .collect(Collectors.groupingBy(ItemResultadoLaboratorialEntity::getResultadoExameId));

        List<ResultadoExame> conteudo = resultado.getContent().stream()
                .map(e -> mapper.paraDominio(e, itensPorResultado.getOrDefault(e.getId(), List.of())))
                .toList();

        return PaginaResultado.de(conteudo, pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<ResultadoExameEntity> construirFiltro(ResultadoExameFiltro filtro) {
        Specification<ResultadoExameEntity> spec = Specification.unrestricted();

        if (filtro.pacienteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataResultado"), filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataResultado"), filtro.periodoFim()));
        }

        return spec;
    }
}