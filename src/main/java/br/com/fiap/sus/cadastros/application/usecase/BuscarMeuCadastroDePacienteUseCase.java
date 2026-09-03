package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vinculo usuario -> paciente da sessao. E como o cliente descobre o proprio pacienteId. */
@Service
public class BuscarMeuCadastroDePacienteUseCase {

    private final PacienteRepository pacientes;
    private final IamQuery iam;
    private final UsuarioAutenticadoProvider autenticado;

    public BuscarMeuCadastroDePacienteUseCase(PacienteRepository pacientes, IamQuery iam,
                                              UsuarioAutenticadoProvider autenticado) {
        this.pacientes = pacientes;
        this.iam = iam;
        this.autenticado = autenticado;
    }

    @Transactional(readOnly = true)
    public PacienteOutput executar() {
        Paciente paciente = pacientes.porUsuarioId(autenticado.obrigatorio().id())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nao ha cadastro de paciente vinculado a este usuario."));
        UsuarioResumo usuario = iam.resumoDoUsuario(paciente.getUsuarioId()).orElse(null);
        return PacienteOutput.de(paciente,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email());
    }
}
