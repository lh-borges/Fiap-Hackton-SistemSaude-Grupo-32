package br.com.fiap.sus.cadastros.infrastructure.persistence.repository;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.infrastructure.persistence.entity.TipoExameEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoExameJpaRepository extends JpaRepository<TipoExameEntity, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    Page<TipoExameEntity> findByAtivo(boolean ativo, Pageable paginacao);

    Page<TipoExameEntity> findByCategoria(CategoriaExame categoria, Pageable paginacao);

    Page<TipoExameEntity> findByCategoriaAndAtivo(CategoriaExame categoria, boolean ativo, Pageable paginacao);
}
