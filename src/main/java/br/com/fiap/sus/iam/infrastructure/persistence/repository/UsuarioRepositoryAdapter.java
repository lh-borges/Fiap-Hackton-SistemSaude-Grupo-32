package br.com.fiap.sus.iam.infrastructure.persistence.repository;

import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.iam.domain.repository.FiltroUsuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.RoleEntity;
import br.com.fiap.sus.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.sus.iam.infrastructure.persistence.mapper.UsuarioPersistenceMapper;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/** Adapter JPA da porta {@link UsuarioRepository}. */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;
    private final RoleJpaRepository roles;
    private final UsuarioPersistenceMapper mapper;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa, RoleJpaRepository roles,
                                    UsuarioPersistenceMapper mapper) {
        this.jpa = jpa;
        this.roles = roles;
        this.mapper = mapper;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.paraDominio(jpa.save(mapper.paraEntidade(usuario, rolesPersistidas())));
    }

    @Override
    public Optional<Usuario> porId(UUID id) {
        return jpa.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Optional<Usuario> porEmail(Email email) {
        return jpa.findByEmail(email.valor()).map(mapper::paraDominio);
    }

    @Override
    public boolean existePorCpf(Cpf cpf) {
        return jpa.existsByCpf(cpf.valor());
    }

    @Override
    public boolean existePorEmail(Email email) {
        return jpa.existsByEmail(email.valor());
    }

    @Override
    public boolean existeOutroComEmail(UUID idAtual, Email email) {
        return jpa.existsByEmailAndIdNot(email.valor(), idAtual);
    }

    @Override
    public PaginaResultado<Usuario> buscar(FiltroUsuario filtro, int pagina, int tamanho) {
        Page<UsuarioEntity> resultado = jpa.findAll(comFiltro(filtro),
                PageRequest.of(pagina, tamanho, Sort.by("nome").ascending()));
        return PaginaResultado.de(resultado.getContent().stream().map(mapper::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private Specification<UsuarioEntity> comFiltro(FiltroUsuario filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (filtro.termo() != null) {
                String like = "%" + filtro.termo().toLowerCase() + "%";
                String digitos = filtro.termo().replaceAll("\\D", "");
                List<Predicate> alternativas = new ArrayList<>();
                alternativas.add(cb.like(cb.lower(root.get("nome")), like));
                alternativas.add(cb.like(cb.lower(root.get("email")), like));
                if (!digitos.isEmpty()) {
                    alternativas.add(cb.like(root.get("cpf"), digitos + "%"));
                }
                condicoes.add(cb.or(alternativas.toArray(new Predicate[0])));
            }
            if (filtro.role() != null) {
                if (query != null) {
                    query.distinct(true);
                }
                Join<UsuarioEntity, RoleEntity> juncao = root.join("roles");
                condicoes.add(cb.equal(juncao.get("nome"), filtro.role()));
            }
            if (filtro.ativo() != null) {
                condicoes.add(cb.equal(root.get("ativo"), filtro.ativo()));
            }
            return condicoes.isEmpty() ? cb.conjunction() : cb.and(condicoes.toArray(new Predicate[0]));
        };
    }

    private Map<RoleNome, RoleEntity> rolesPersistidas() {
        Map<RoleNome, RoleEntity> mapa = new EnumMap<>(RoleNome.class);
        roles.findAll().forEach(role -> mapa.put(role.getNome(), role));
        return mapa;
    }
}
