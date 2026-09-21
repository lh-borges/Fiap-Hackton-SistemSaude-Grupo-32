package br.com.fiap.sus.documentos.infrastructure.persistence.repository;

import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoFiltro;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.documentos.infrastructure.persistence.entity.DocumentoMedicoEntity;
import br.com.fiap.sus.documentos.infrastructure.persistence.mapper.DocumentoPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class DocumentoMedicoRepositoryAdapter implements DocumentoMedicoRepository {
    private final DocumentoMedicoJpaRepository repository;
    private final DocumentoPersistenceMapper mapper;

    public DocumentoMedicoRepositoryAdapter(
            DocumentoMedicoJpaRepository repository,
            DocumentoPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public DocumentoMedico salvar(DocumentoMedico documento) {
        return mapper.paraDominio(repository.save(mapper.paraEntidade(documento)));
    }

    @Override
    public Optional<DocumentoMedico> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorConsultaId(UUID consultaId) {
        return repository.existsByConsultaId(consultaId);
    }

    @Override
    public PaginaResultado<DocumentoMedico> listar(DocumentoMedicoFiltro filtro, int pagina, int tamanho) {
        Specification<DocumentoMedicoEntity> spec = Specification.unrestricted();
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
        if (filtro.tipo() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), filtro.tipo()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEmissao"),
                    filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEmissao"),
                    filtro.periodoFim()));
        }
        var page = repository.findAll(spec,
                PageRequest.of(pagina, tamanho, Sort.by("dataEmissao").descending().and(Sort.by("id"))));
        return PaginaResultado.de(page.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, page.getTotalElements());
    }
}
