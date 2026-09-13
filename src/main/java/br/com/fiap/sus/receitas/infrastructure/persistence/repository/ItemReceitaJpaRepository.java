package br.com.fiap.sus.receitas.infrastructure.persistence.repository;

import br.com.fiap.sus.receitas.infrastructure.persistence.entity.ItemReceitaEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemReceitaJpaRepository extends JpaRepository<ItemReceitaEntity, UUID> {

    List<ItemReceitaEntity> findByReceitaId(UUID receitaId);

    List<ItemReceitaEntity> findByReceitaIdIn(Collection<UUID> receitaIds);
}