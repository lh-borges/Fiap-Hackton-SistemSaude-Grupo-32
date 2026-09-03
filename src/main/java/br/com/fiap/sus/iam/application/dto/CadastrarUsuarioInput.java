package br.com.fiap.sus.iam.application.dto;

import java.util.Set;

public record CadastrarUsuarioInput(String nome, String cpf, String email, String senha, Set<String> roles) {
}
