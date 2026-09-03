/**
 * Modulo de cadastros: paciente, medico e catalogos de apoio (especialidade,
 * unidade de saude e tipo de exame).
 *
 * <p>Depende de iam apenas pela porta publicada {@code iam::api}, para validar que o
 * usuario existe e possui o perfil correto. Nao ha FK para as tabelas de iam.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Cadastros",
        allowedDependencies = {"shared", "iam::api"})
package br.com.fiap.sus.cadastros;
