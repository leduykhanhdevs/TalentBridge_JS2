package vn.talentbridge.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalArchitectureTest {

    private static final JavaClasses CORE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("vn.talentbridge.core");

    private static final JavaClasses ALL = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("vn.talentbridge");

    @Test
    @DisplayName("Core domain & application must not depend on Spring Framework")
    void coreMustNotDependOnSpringFramework() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.core..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .allowEmptyShould(true);
        rule.check(CORE);
    }

    @Test
    @DisplayName("Core domain & application must not depend on JPA/Hibernate")
    void coreMustNotDependOnJpa() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.persistence..", "javax.persistence..", "org.hibernate..")
                .allowEmptyShould(true);
        rule.check(CORE);
    }

    @Test
    @DisplayName("Core domain & application must not depend on Servlet or JWT")
    void coreMustNotDependOnServletOrJwt() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "javax.servlet..", "io.jsonwebtoken..")
                .allowEmptyShould(true);
        rule.check(CORE);
    }

    @Test
    @DisplayName("Core domain & application must not depend on Adapters or Config")
    void coreMustNotDependOnAdapterOrConfig() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("vn.talentbridge.adapter..", "vn.talentbridge.config..")
                .allowEmptyShould(true);
        rule.check(CORE);
    }

    @Test
    @DisplayName("Domain models must not depend on Application layer")
    void domainMustNotDependOnApplication() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.core.domain..")
                .should().dependOnClassesThat().resideInAPackage("vn.talentbridge.core.application..")
                .allowEmptyShould(true);
        rule.check(CORE);
    }

    @Test
    @DisplayName("Inbound web adapters must not depend on Outbound persistence adapters")
    void inboundAdaptersMustNotDependOnOutboundAdapters() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.adapter.in..")
                .should().dependOnClassesThat().resideInAPackage("vn.talentbridge.adapter.out..")
                .allowEmptyShould(true);
        rule.check(ALL);
    }

    @Test
    @DisplayName("Inbound controllers must only depend on Inbound Ports, not Concrete UseCase Implementations")
    void inboundAdaptersMustNotDependOnConcreteUseCaseImplementations() {
        ArchRule rule = noClasses().that().resideInAPackage("vn.talentbridge.adapter.in..")
                .should().dependOnClassesThat().resideInAPackage("vn.talentbridge.core.application.usecase..")
                .allowEmptyShould(true);
        rule.check(ALL);
    }

    @Test
    @DisplayName("Outbound repository adapters must implement Outbound Ports")
    void outboundAdaptersMustImplementOutboundPorts() {
        ArchRule rule = classes().that().resideInAPackage("vn.talentbridge.adapter.out.persistence.adapter..")
                .and().areNotInterfaces()
                .should().implement(resideInAPackage("vn.talentbridge.core.application.port.out.."));
        rule.check(ALL);
    }
}