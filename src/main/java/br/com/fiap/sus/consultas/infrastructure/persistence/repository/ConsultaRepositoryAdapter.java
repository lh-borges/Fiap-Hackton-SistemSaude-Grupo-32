package br.com.fiap.sus.consultas.infrastructure.persistence.repository;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaFiltro;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.consultas.infrastructure.persistence.entity.ConsultaEntity;
import br.com.fiap.sus.consultas.infrastructure.persistence.mapper.ConsultaPersistenceMapper;
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
public class ConsultaRepositoryAdapter implements ConsultaRepository {

    private final ConsultaJpaRepository jpa;
    private final ConsultaPersistenceMapper mapper;

    public ConsultaRepositoryAdapter(ConsultaJpaRepository jpa, ConsultaPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Consulta salvar(Consulta consulta) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(consulta)));
    }

    @Override
    public Optional<Consulta> buscarPorId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public PaginaResultado<Consulta> listar(ConsultaFiltro filtro, int pagina, int tamanho) {
        Specification<ConsultaEntity> spec = construirFiltro(filtro);
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("dataHora").descending());
        Page<ConsultaEntity> resultado = jpa.findAll(spec, paginacao);
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<ConsultaEntity> construirFiltro(ConsultaFiltro filtro) {
        Specification<ConsultaEntity> spec = Specification.unrestricted();

        if (filtro.pacienteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.medicoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("medicoId"), filtro.medicoId()));
        }
        if (filtro.unidadeSaudeId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unidadeSaudeId"), filtro.unidadeSaudeId()));
        }
        if (filtro.situacao() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("situacao"), filtro.situacao()));
        }
        if (filtro.periodoInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataHora"), filtro.periodoInicio()));
        }
        if (filtro.periodoFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataHora"), filtro.periodoFim()));
        }

        return spec;
    }
}