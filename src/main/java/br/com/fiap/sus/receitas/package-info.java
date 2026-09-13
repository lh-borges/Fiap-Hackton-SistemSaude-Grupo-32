/**
 * Modulo de receitas: prescricao emitida por um medico para um paciente, com
 * suporte a renovacao e cancelamento.
 *
 * <p>Depende de cadastros apenas pela porta publicada {@code cadastros::api}, para
 * validar paciente e medico. Nao ha FK para as tabelas de cadastros.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Receitas",
        allowedDependencies = {"shared", "cadastros::api"})
package br.com.fiap.sus.receitas;