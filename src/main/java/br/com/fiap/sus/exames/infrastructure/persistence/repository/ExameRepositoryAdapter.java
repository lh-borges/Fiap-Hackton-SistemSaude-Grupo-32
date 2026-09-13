package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.domain.enums.SituacaoExame;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.repository.ExameFiltro;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.ExameEntity;
import br.com.fiap.sus.exames.infrastructure.persistence.entity.SolicitacaoExameEntity;
import br.com.fiap.sus.exames.infrastructure.persistence.mapper.ExamePersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class ExameRepositoryAdapter implements ExameRepository {

    private final ExameJpaRepository jpa;
    private final ExamePersistenceMapper mapper;

    public ExameRepositoryAdapter(ExameJpaRepository jpa, ExamePersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Exame salvar(Exame exame) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(exame)));
    }

    @Override
    public Optional<Exame> buscarPorId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existeExameAtivoParaSolicitacao(UUID solicitacaoExameId) {
        return jpa.existsBySolicitacaoExameIdAndSituacaoNot(solicitacaoExameId, SituacaoExame.CANCELADO);
    }

    @Override
    public Optional<Exame> buscarPorSolicitacaoId(UUID solicitacaoExameId) {
        return jpa.findBySolicitacaoExameId(solicitacaoExameId).map(mapper::paraDominio);
    }

    @Override
    public PaginaResultado<Exame> listar(ExameFiltro filtro, int pagina, int tamanho) {
        Specification<ExameEntity> spec = construirFiltro(filtro);
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("dataAgendada").descending());
        Page<ExameEntity> resultado = jpa.findAll(spec, paginacao);
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<ExameEntity> construirFiltro(ExameFiltro filtro) {
        Specification<ExameEntity> spec = Specification.unrestricted();

        if (filtro.pacienteId() != null) {
            // Exame nao guarda pacienteId diretamente; resolve via subquery em
            // SolicitacaoExameEntity, que pertence ao mesmo modulo (join interno permitido).
            spec = spec.and((root, query, cb) -> {
                var subquery = query.subquery(UUID.class);
                var solicitacaoRoot = subquery.from(SolicitacaoExameEntity.class);
                subquery.select(solicitacaoRoot.get("id"))
                        .where(cb.equal(solicitacaoRoot.get("pacienteId"), filtro.pacienteId()));
                return root.get("solicitacaoExameId").in(subquery);
            });
        }
        if (filtro.unidadeSaudeId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unidadeSaudeId"), filtro.unidadeSaudeId()));
        }
        if (filtro.situacao() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("situacao"), filtro.situacao()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataAgendada"), filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataAgendada"), filtro.periodoFim()));
        }

        return spec;
    }
}