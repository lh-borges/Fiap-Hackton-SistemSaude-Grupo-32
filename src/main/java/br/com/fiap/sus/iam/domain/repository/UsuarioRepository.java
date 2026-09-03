package br.com.fiap.sus.iam.domain.repository;

import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.vo.Cpf;
import br.com.fiap.sus.shared.domain.vo.Email;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistencia de usuario (Artigo II.4). */
public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> porId(UUID id);

    Optional<Usuario> porEmail(Email email);

    boolean existePorCpf(Cpf cpf);

    boolean existePorEmail(Email email);

    boolean existeOutroComEmail(UUID idAtual, Email email);

    PaginaResultado<Usuario> buscar(FiltroUsuario filtro, int pagina, int tamanho);
}
