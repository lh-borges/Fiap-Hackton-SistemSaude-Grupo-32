package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-04: verificacao de posse. Um paciente que consulta o cadastro de outro recebe
 * a mesma resposta de um cadastro inexistente (Artigo IV.7).
 */
@Service
public class BuscarPacienteUseCase {

    private final PacienteRepository pacientes;
    private final IamQuery iam;
    private final UsuarioAutenticadoProvider autenticado;

    public BuscarPacienteUseCase(PacienteRepository pacientes, IamQuery iam,
                                 UsuarioAutenticadoProvider autenticado) {
        this.pacientes = pacientes;
        this.iam = iam;
        this.autenticado = autenticado;
    }

    @Transactional(readOnly = true)
    public PacienteOutput executar(UUID id) {
        UsuarioAutenticado solicitante = autenticado.obrigatorio();
        Paciente paciente = pacientes.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente"));

        boolean apenasPaciente = solicitante.ehPaciente() && !solicitante.ehAdministrador()
                && !solicitante.temRole(UsuarioAutenticado.ATENDENTE)
                && !solicitante.ehMedico();
        if (apenasPaciente && !paciente.pertenceAoUsuario(solicitante.id())) {
            throw RecursoNaoEncontradoException.de("Paciente");
        }

        UsuarioResumo usuario = iam.resumoDoUsuario(paciente.getUsuarioId()).orElse(null);
        return PacienteOutput.de(paciente,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email());
    }
}
