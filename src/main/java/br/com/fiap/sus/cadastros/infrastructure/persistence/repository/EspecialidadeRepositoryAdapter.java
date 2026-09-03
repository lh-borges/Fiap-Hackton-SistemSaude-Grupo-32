package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.EspecialidadeEntity;
import br.com.fiap.sus.cadastros.infrastructure.persistence.mapper.CadastroPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class EspecialidadeRepositoryAdapter implements EspecialidadeRepository {

    private final EspecialidadeJpaRepository jpa;
    private final CadastroPersistenceMapper mapper;

    public EspecialidadeRepositoryAdapter(EspecialidadeJpaRepository jpa, CadastroPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Especialidade salvar(Especialidade especialidade) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(especialidade)));
    }

    @Override
    public Optional<Especialidade> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public PaginaResultado<Especialidade> listar(Boolean ativo, int pagina, int tamanho) {
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        Page<EspecialidadeEntity> resultado = ativo == null
                ? jpa.findAll(paginacao)
                : jpa.findByAtivo(ativo, paginacao);
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }
}
