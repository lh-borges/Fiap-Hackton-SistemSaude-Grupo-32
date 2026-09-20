package br.com.fiap.sus.pareceres.infrastructure.persistence.repository;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.pareceres.infrastructure.persistence.entity.ParecerMedicoEntity;
import br.com.fiap.sus.pareceres.infrastructure.persistence.mapper.ParecerPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
public class ParecerMedicoRepositoryAdapter implements ParecerMedicoRepository {
    private final ParecerMedicoJpaRepository repository;
    private final ParecerPersistenceMapper mapper;
    private final EntityManager entityManager;

    public ParecerMedicoRepositoryAdapter(
            ParecerMedicoJpaRepository repository,
            ParecerPersistenceMapper mapper,
            EntityManager entityManager
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ParecerMedico salvar(ParecerMedico parecer, UUID criadoPorUsuarioId) {
        entityManager.persist(mapper.paraEntidade(parecer, requireNonNull(criadoPorUsuarioId, "Usuário emissor é obrigatório.")));
        return parecer;
    }

    @Override
    public Set<UUID> resultadosComParecer(Set<UUID> ids) {
        return ids.isEmpty() ? java.util.Set.of() : repository.resultadosComParecer(ids);
    }

    @Override
    public Optional<ParecerMedico> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorResultadoExameId(UUID resultadoExameId) {
        return repository.existsByResultadoExameId(resultadoExameId);
    }

    @Override
    public PaginaResultado<ParecerMedico> listar(ParecerMedicoFiltro filtro, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Pagina deve ser positiva ou zero e tamanho entre 1 e 100.");
        }
        Specification<ParecerMedicoEntity> spec = Specification.unrestricted();
        if (filtro.pacientesPermitidos() != null) {
            spec = spec.and((root, query, cb) -> filtro.pacientesPermitidos().isEmpty()
                    ? cb.disjunction() : root.get("pacienteId").in(filtro.pacientesPermitidos()));
        }
        if (filtro.pacienteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.medicoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("medicoId"), filtro.medicoId()));
        }
        if (filtro.resultadoExameId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("resultadoExameId"), filtro.resultadoExameId()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataParecer"), filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataParecer"), filtro.periodoFim()));
        }
        var page = repository.findAll(spec,
                PageRequest.of(pagina, tamanho, Sort.by("dataParecer").descending().and(Sort.by("id"))));
        return PaginaResultado.de(page.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, page.getTotalElements());
    }
}
