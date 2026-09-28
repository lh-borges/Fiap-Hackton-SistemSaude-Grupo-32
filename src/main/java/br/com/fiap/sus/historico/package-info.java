/**
 * Historico do paciente (feature 010): linha do tempo derivada em tempo de leitura das portas
 * publicadas pelos modulos clinicos. Sem tabela, sem migration, sem evento.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Historico",
        allowedDependencies = {
                "shared", "cadastros::api", "consultas::api", "exames::api", "resultados::api",
                "pareceres::api", "receitas::api", "documentos::api"})
package br.com.fiap.sus.historico;
