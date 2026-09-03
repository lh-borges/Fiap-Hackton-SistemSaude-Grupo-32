package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.PacienteEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.mapper.CadastroPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class PacienteRepositoryAdapter implements PacienteRepository {

    private final PacienteJpaRepository jpa;
    private final CadastroPersistenceMapper mapper;

    public PacienteRepositoryAdapter(PacienteJpaRepository jpa, CadastroPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Paciente salvar(Paciente paciente) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(paciente)));
    }

    @Override
    public Optional<Paciente> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Optional<Paciente> porUsuarioId(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorCartaoSus(String cartaoSus) {
        return jpa.existsByCartaoSus(cartaoSus);
    }

    @Override
    public boolean existePorUsuarioId(UUID usuarioId) {
        return jpa.existsByUsuarioId(usuarioId);
    }

    @Override
    public PaginaResultado<Paciente> buscar(String cartaoSus, Collection<UUID> usuarioIds, Boolean ativo,
                                            int pagina, int tamanho) {
        Page<PacienteEntity> resultado = jpa.findAll(comFiltro(cartaoSus, usuarioIds, ativo),
                PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending()));
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<PacienteEntity> comFiltro(String termo, Collection<UUID> usuarioIds, Boolean ativo) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (termo != null) {
                List<Predicate> alternativas = new ArrayList<>();
                String digitos = termo.replaceAll("\\D", "");
                if (!digitos.isEmpty()) {
                    alternativas.add(cb.like(root.get("cartaoSus"), digitos + "%"));
                }
                if (usuarioIds != null && !usuarioIds.isEmpty()) {
                    alternativas.add(root.get("usuarioId").in(usuarioIds));
                }
                // Termo informado sem nenhuma correspondencia possivel: nao retorna nada.
                condicoes.add(alternativas.isEmpty() ? cb.disjunction()
                        : cb.or(alternativas.toArray(new Predicate[0])));
            }
            if (ativo != null) {
                condicoes.add(cb.equal(root.get("ativo"), ativo));
            }
            return condicoes.isEmpty() ? cb.conjunction() : cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
