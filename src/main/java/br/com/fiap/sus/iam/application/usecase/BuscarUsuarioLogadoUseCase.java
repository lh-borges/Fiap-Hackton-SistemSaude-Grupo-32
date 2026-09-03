package br.com.fiap.sus.iam.application.usecase;

import br.com.fiap.sus.iam.application.dto.UsuarioOutput;
import br.com.fiap.sus.iam.domain.repository.UsuarioRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-06: dados do proprio usuario autenticado. */
@Service
public class BuscarUsuarioLogadoUseCase {

    private final UsuarioRepository usuarios;
    private final UsuarioAutenticadoProvider autenticado;

    public BuscarUsuarioLogadoUseCase(UsuarioRepository usuarios, UsuarioAutenticadoProvider autenticado) {
        this.usuarios = usuarios;
        this.autenticado = autenticado;
    }

    @Transactional(readOnly = true)
    public UsuarioOutput executar() {
        return usuarios.porId(autenticado.obrigatorio().id())
                .map(UsuarioOutput::de)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario"));
    }
}
