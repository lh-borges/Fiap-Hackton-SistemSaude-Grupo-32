/**
 * Modulo de identidade e acesso: usuario, perfis, autenticacao e emissao de token.
 * Nao depende de nenhum outro modulo de negocio.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "IAM",
        allowedDependencies = "shared")
package br.com.fiap.sus.iam;
