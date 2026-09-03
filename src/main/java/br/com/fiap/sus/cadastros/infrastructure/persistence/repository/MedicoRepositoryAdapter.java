package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.MedicoEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.mapper.CadastroPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class MedicoRepositoryAdapter implements MedicoRepository {

    private final MedicoJpaRepository jpa;
    private final CadastroPersistenceMapper mapper;

    public MedicoRepositoryAdapter(MedicoJpaRepository jpa, CadastroPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Medico salvar(Medico medico) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(medico)));
    }

    @Override
    public Optional<Medico> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Optional<Medico> porUsuarioId(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorCrmEUf(String crm, String ufCrm) {
        return jpa.existsByCrmAndUfCrm(crm, ufCrm);
    }

    @Override
    public boolean existePorUsuarioId(UUID usuarioId) {
        return jpa.existsByUsuarioId(usuarioId);
    }

    @Override
    public PaginaResultado<Medico> buscar(UUID especialidadeId, Boolean ativo, int pagina, int tamanho) {
        Page<MedicoEntity> resultado = jpa.findAll(comFiltro(especialidadeId, ativo),
                PageRequest.of(pagina, tamanho, Sort.by("crm").ascending()));
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<MedicoEntity> comFiltro(UUID especialidadeId, Boolean ativo) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (especialidadeId != null) {
                condicoes.add(cb.equal(root.get("especialidadeId"), especialidadeId));
            }
            if (ativo != null) {
                condicoes.add(cb.equal(root.get("ativo"), ativo));
            }
            return condicoes.isEmpty() ? cb.conjunction() : cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
