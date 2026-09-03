package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.CadastrarMedicoInput;
import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-02: cadastro de medico. Exige usuario ativo com perfil MEDICO e especialidade ativa. */
@Service
public class CadastrarMedicoUseCase {

    private final MedicoRepository medicos;
    private final EspecialidadeRepository especialidades;
    private final IamQuery iam;

    public CadastrarMedicoUseCase(MedicoRepository medicos, EspecialidadeRepository especialidades,
                                  IamQuery iam) {
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.iam = iam;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MedicoOutput executar(CadastrarMedicoInput input) {
        if (!iam.usuarioAtivoPossuiRole(input.usuarioId(), "MEDICO")) {
            throw new RegraDeNegocioException(
                    "O usuario informado nao existe, esta inativo ou nao possui o perfil MEDICO.");
        }
        if (medicos.existePorUsuarioId(input.usuarioId())) {
            throw new ConflitoException("Este usuario ja possui cadastro de medico.");
        }

        Especialidade especialidade = especialidades.porId(input.especialidadeId())
                .filter(Especialidade::isAtivo)
                .orElseThrow(() -> new RegraDeNegocioException("Especialidade inexistente ou inativa."));

        Medico medico = Medico.criar(input.usuarioId(), input.crm(), input.ufCrm(), especialidade.getId());
        if (medicos.existePorCrmEUf(medico.getCrm(), medico.getUfCrm())) {
            throw new ConflitoException("Ja existe um medico com este CRM nesta UF.");
        }

        Medico salvo = medicos.salvar(medico);
        UsuarioResumo usuario = iam.resumoDoUsuario(salvo.getUsuarioId()).orElse(null);
        return MedicoOutput.de(salvo,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email(),
                especialidade.getNome());
    }
}
