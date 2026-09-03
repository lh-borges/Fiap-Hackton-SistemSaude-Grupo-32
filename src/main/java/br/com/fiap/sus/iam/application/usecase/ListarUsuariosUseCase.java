package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.domain.enums.RoleNome;
import br.com.fiap.sus.iam.domain.repository.FiltroUsuario;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-02: listagem paginada de usuarios, restrita ao administrador. */
@Service
public class ListarUsuariosUseCase {

    private static final int TAMANHO_MAXIMO = 100;

    private final UsuarioRepository usuarios;

    public ListarUsuariosUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public PaginaResultado<UsuarioOutput> executar(String termo, String role, Boolean ativo,
                                                   int pagina, int tamanho) {
        FiltroUsuario filtro = new FiltroUsuario(
                termo == null || termo.isBlank() ? null : termo.trim(),
                role == null || role.isBlank() ? null : RoleNome.de(role),
                ativo);
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return usuarios.buscar(filtro, Math.max(pagina, 0), tamanhoValido).mapear(UsuarioOutput::de);
    }
}
