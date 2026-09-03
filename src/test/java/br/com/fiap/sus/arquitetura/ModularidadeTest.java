package br.com.fiap.sus.arquitetura;

import br.com.fiap.sus.SusApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Artigo III: as fronteiras entre modulos sao verificadas no build.
 * Quebra se um modulo acessar pacote interno de outro ou criar dependencia nao declarada.
 */
@DisplayName("Fronteiras de modulo (Spring Modulith)")
class ModularidadeTest {

    @Test
    @DisplayName("nenhum modulo viola as dependencias declaradas")
    void fronteirasRespeitadas() {
        ApplicationModules.of(SusApplication.class).verify();
    }
}
