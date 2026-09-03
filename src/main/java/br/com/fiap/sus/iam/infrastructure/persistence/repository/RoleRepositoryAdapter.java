package br.com.fiap.sus.iam.infrastructure.persistence.repository;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Role;
import br.com.fiap.sus.iam.domain.repository.RoleRepository;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.RoleEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpa;

    public RoleRepositoryAdapter(RoleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Role> listarTodas() {
        return jpa.findAll().stream()
                .map(this::converter)
                .sorted(Comparator.comparing(role -> role.nome().name()))
                .toList();
    }

    @Override
    public Optional<Role> porNome(RoleNome nome) {
        return jpa.findByNome(nome).map(this::converter);
    }

    private Role converter(RoleEntity entidade) {
        return new Role(entidade.getId(), entidade.getNome());
    }
}
