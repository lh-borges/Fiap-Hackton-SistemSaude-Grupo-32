package br.com.fiap.sus.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacao da API (Artigo VIII.5). O esquema Bearer habilita o botao Authorize no Swagger UI. */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Digital de Atendimento SUS")
                        .version("v1")
                        .description("""
                                API do Hackathon FIAP - Grupo 32.

                                **Como usar:** autentique em `POST /api/v1/auth/login`, copie o campo
                                `token` da resposta e informe em **Authorize** (o prefixo `Bearer` e
                                adicionado automaticamente). Todas as demais rotas exigem o token.

                                Perfis: ADMINISTRADOR, ATENDENTE, MEDICO e PACIENTE. O paciente so
                                enxerga os proprios dados.
                                """)
                        .contact(new Contact().name("Grupo 32 - FIAP"))
                        .license(new License().name("Uso academico")))
                .servers(List.of(new Server().url("/").description("Servidor atual")))
                .tags(List.of(
                        new Tag().name("Autenticacao").description("Login e dados do usuario da sessao"),
                        new Tag().name("Usuarios").description("Gestao de usuarios e perfis de acesso"),
                        new Tag().name("Pacientes").description("Cadastro de pacientes"),
                        new Tag().name("Medicos").description("Cadastro de medicos"),
                        new Tag().name("Especialidades").description("Catalogo de especialidades medicas"),
                        new Tag().name("Unidades de saude").description("Catalogo de unidades de saude"),
                        new Tag().name("Tipos de exame").description("Catalogo de tipos de exame")))
                .components(new Components().addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT obtido em POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
