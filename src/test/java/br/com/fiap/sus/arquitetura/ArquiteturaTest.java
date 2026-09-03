package br.com.fiap.sus.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Artigo II: a regra da dependencia e verificada no build, nao na revisao de codigo. */
@DisplayName("Clean Architecture")
class ArquiteturaTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.fiap.sus");
    }

    @Test
    @DisplayName("o dominio nao depende do Spring")
    void dominioNaoDependeDoSpring() {
        ArchRule regra = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");
        regra.check(classes);
    }

    @Test
    @DisplayName("o dominio nao depende de JPA")
    void dominioNaoDependeDeJpa() {
        ArchRule regra = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");
        regra.check(classes);
    }

    @Test
    @DisplayName("o dominio nao depende da infraestrutura nem da apresentacao")
    void dominioNaoDependeDasCamadasExternas() {
        ArchRule regra = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..");
        regra.check(classes);
    }

    @Test
    @DisplayName("a aplicacao nao depende da apresentacao")
    void aplicacaoNaoDependeDaApresentacao() {
        ArchRule regra = noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..presentation..");
        regra.check(classes);
    }

    @Test
    @DisplayName("o controller nao acessa a persistencia diretamente")
    void controllerNaoAcessaPersistencia() {
        ArchRule regra = noClasses().that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.persistence..");
        regra.check(classes);
    }

    @Test
    @DisplayName("entidade JPA so existe na camada de persistencia")
    void entidadeJpaSoNaPersistencia() {
        ArchRule regra = classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..infrastructure.persistence.entity..");
        regra.check(classes);
    }

    @Test
    @DisplayName("controller REST so existe na camada de apresentacao")
    void controllerSoNaApresentacao() {
        ArchRule regra = classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage("..presentation.controller..");
        regra.check(classes);
    }
}
