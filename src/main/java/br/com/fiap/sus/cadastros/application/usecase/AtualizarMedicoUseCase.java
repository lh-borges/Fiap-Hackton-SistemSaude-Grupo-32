package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Altera a especialidade do medico. CRM e UF sao imutaveis apos o cadastro. */
@Service
public class AtualizarMedicoUseCase {

    private final MedicoRepository medicos;
    private final EspecialidadeRepository especialidades;
    private final IamQuery iam;

    public AtualizarMedicoUseCase(MedicoRepository medicos, EspecialidadeRepository especialidades,
                                  IamQuery iam) {
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.iam = iam;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MedicoOutput executar(UUID id, UUID especialidadeId) {
        Medico medico = medicos.porId(id).orElseThrow(() -> RecursoNaoEncontradoException.de("Medico"));
        Especialidade especialidade = especialidades.porId(especialidadeId)
                .filter(Especialidade::isAtivo)
                .orElseThrow(() -> new RegraDeNegocioException("Especialidade inexistente ou inativa."));

        medico.alterarEspecialidade(especialidade.getId());
        Medico salvo = medicos.salvar(medico);
        UsuarioResumo usuario = iam.resumoDoUsuario(salvo.getUsuarioId()).orElse(null);
        return MedicoOutput.de(salvo,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email(),
                especialidade.getNome());
    }
}
