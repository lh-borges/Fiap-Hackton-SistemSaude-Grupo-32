package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.UnidadeSaudeEntity;
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
public class UnidadeSaudeRepositoryAdapter implements UnidadeSaudeRepository {

    private final UnidadeSaudeJpaRepository jpa;
    private final CadastroPersistenceMapper mapper;

    public UnidadeSaudeRepositoryAdapter(UnidadeSaudeJpaRepository jpa, CadastroPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public UnidadeSaude salvar(UnidadeSaude unidade) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(unidade)));
    }

    @Override
    public Optional<UnidadeSaude> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorCnes(String cnes) {
        return jpa.existsByCnes(cnes);
    }

    @Override
    public PaginaResultado<UnidadeSaude> listar(Boolean ativo, int pagina, int tamanho) {
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        Page<UnidadeSaudeEntity> resultado = ativo == null
                ? jpa.findAll(paginacao)
                : jpa.findByAtivo(ativo, paginacao);
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }
}
