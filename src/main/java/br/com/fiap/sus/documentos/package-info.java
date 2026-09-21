/** Documentos medicos emitidos durante o atendimento. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Documentos",
        allowedDependencies = {"shared", "cadastros::api", "consultas::api", "exames::api", "resultados::api"})
package br.com.fiap.sus.documentos;
