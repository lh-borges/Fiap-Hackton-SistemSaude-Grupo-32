/** Interpretacao medica independente do resultado tecnico do exame. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Pareceres",
        allowedDependencies = {"shared", "cadastros::api", "resultados::api", "consultas::api", "iam::api"})
package br.com.fiap.sus.pareceres;
