package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.TipoExameEntity;
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
public class TipoExameRepositoryAdapter implements TipoExameRepository {

    private final TipoExameJpaRepository jpa;
    private final CadastroPersistenceMapper mapper;

    public TipoExameRepositoryAdapter(TipoExameJpaRepository jpa, CadastroPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public TipoExame salvar(TipoExame tipoExame) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(tipoExame)));
    }

    @Override
    public Optional<TipoExame> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public PaginaResultado<TipoExame> listar(CategoriaExame categoria, Boolean ativo, int pagina, int tamanho) {
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        Page<TipoExameEntity> resultado;
        if (categoria != null && ativo != null) {
            resultado = jpa.findByCategoriaAndAtivo(categoria, ativo, paginacao);
        } else if (categoria != null) {
            resultado = jpa.findByCategoria(categoria, paginacao);
        } else if (ativo != null) {
            resultado = jpa.findByAtivo(ativo, paginacao);
        } else {
            resultado = jpa.findAll(paginacao);
        }
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }
}
