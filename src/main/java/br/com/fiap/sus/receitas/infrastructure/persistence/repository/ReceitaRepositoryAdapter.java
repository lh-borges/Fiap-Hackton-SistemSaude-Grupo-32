package br.com.fiap.sus.receitas.infrastructure.persistence.repository;

import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaFiltro;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ItemReceitaEntity;
import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ReceitaEntity;
import br.com.fiap.sus.receitas.infrastructure.persistence.mapper.ReceitaPersistenceMapper;
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
public class ReceitaRepositoryAdapter implements ReceitaRepository {

    private final ReceitaJpaRepository receitaJpa;
    private final ItemReceitaJpaRepository itemJpa;
    private final ReceitaPersistenceMapper mapper;

    public ReceitaRepositoryAdapter(ReceitaJpaRepository receitaJpa, ItemReceitaJpaRepository itemJpa,
                                    ReceitaPersistenceMapper mapper) {
        this.receitaJpa = receitaJpa;
        this.itemJpa = itemJpa;
        this.mapper = mapper;
    }

    /** @Transactional necessario: salvar receita + itens sao chamadas separadas
     *  (mesmo motivo de ResultadoExameRepositoryAdapter - RN-02 exige ao menos 1 item). */
    @Override
    @Transactional
    public Receita salvar(Receita receita) {
        ReceitaEntity entidadeSalva = receitaJpa.save(mapper.paraEntidade(receita));

        var itensEntity = receita.getItens().stream()
                .map(item -> mapper.paraEntidade(entidadeSalva.getId(), item))
                .toList();
        itemJpa.saveAll(itensEntity);

        return mapper.paraDominio(entidadeSalva, itemJpa.findByReceitaId(entidadeSalva.getId()));
    }

    @Override
    public Optional<Receita> buscarPorId(UUID id) {
        return receitaJpa.findById(id)
                .map(e -> mapper.paraDominio(e, itemJpa.findByReceitaId(e.getId())));
    }

    @Override
    public PaginaResultado<Receita> listar(ReceitaFiltro filtro, int pagina, int tamanho) {
        Specification<ReceitaEntity> spec = construirFiltro(filtro);
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("dataEmissao").descending());
        Page<ReceitaEntity> resultado = receitaJpa.findAll(spec, paginacao);

        List<UUID> idsDaPagina = resultado.getContent().stream().map(ReceitaEntity::getId).toList();

        Map<UUID, List<ItemReceitaEntity>> itensPorReceita = itemJpa.findByReceitaIdIn(idsDaPagina).stream()
                .collect(Collectors.groupingBy(ItemReceitaEntity::getReceitaId));

        List<Receita> conteudo = resultado.getContent().stream()
                .map(e -> mapper.paraDominio(e, itensPorReceita.getOrDefault(e.getId(), List.of())))
                .toList();

        return PaginaResultado.de(conteudo, pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<ReceitaEntity> construirFiltro(ReceitaFiltro filtro) {
        Specification<ReceitaEntity> spec = Specification.unrestricted();

        if (filtro.pacienteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.medicoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("medicoId"), filtro.medicoId()));
        }
        if (filtro.situacao() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("situacao"), filtro.situacao()));
        }

        return spec;
    }
}