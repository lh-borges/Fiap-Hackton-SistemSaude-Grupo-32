package br.com.fiap.sus.iam.presentation.controller;

import br.com.fiap.sus.iam.application.usecase.ListarRolesUseCase;
import br.com.fiap.sus.iam.presentation.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final ListarRolesUseCase listarRoles;

    public RoleController(ListarRolesUseCase listarRoles) {
        this.listarRoles = listarRoles;
    }

    @Operation(summary = "Lista os perfis de autorizacao disponiveis")
    @GetMapping
    public List<RoleResponse> listar() {
        return listarRoles.executar().stream().map(RoleResponse::de).toList();
    }
}
