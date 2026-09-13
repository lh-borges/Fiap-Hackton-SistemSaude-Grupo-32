package br.com.fiap.sus.exames.infrastructure.persistence.repository;

import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameFiltro;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
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
public class SolicitacaoExameRepositoryAdapter implements SolicitacaoExameRepository {

    private final SolicitacaoExameJpaRepository jpa;
    private final ExamePersistenceMapper mapper;

    public SolicitacaoExameRepositoryAdapter(SolicitacaoExameJpaRepository jpa, ExamePersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public SolicitacaoExame salvar(SolicitacaoExame solicitacao) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(solicitacao)));
    }

    @Override
    public Optional<SolicitacaoExame> buscarPorId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public PaginaResultado<SolicitacaoExame> listar(SolicitacaoExameFiltro filtro, int pagina, int tamanho) {
        Specification<SolicitacaoExameEntity> spec = construirFiltro(filtro);
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        Page<SolicitacaoExameEntity> resultado = jpa.findAll(spec, paginacao);
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<SolicitacaoExameEntity> construirFiltro(SolicitacaoExameFiltro filtro) {
        Specification<SolicitacaoExameEntity> spec = Specification.unrestricted();
        if (filtro.pacienteId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.medicoId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("medicoId"), filtro.medicoId()));
        }
        if (filtro.tipoExameId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("tipoExameId"), filtro.tipoExameId()));
        }
        if (filtro.situacao() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("situacao"), filtro.situacao()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("criadoEm"), filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("criadoEm"), filtro.periodoFim()));
        }
        return spec;
    }
}