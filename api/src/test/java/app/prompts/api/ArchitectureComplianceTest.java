package app.prompts.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "app.prompts.api", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Architecture compliance rules")
public class ArchitectureComplianceTest {

    @ArchTest
    @DisplayName("Domain layer remains free of framework and infrastructure dependencies")
    static void domain_should_be_framework_free(JavaClasses classes) {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "..infrastructure.."
            );
        rule.check(classes);
    }

    @ArchTest
    @DisplayName("Application layer does not depend on infrastructure")
    static void application_should_not_depend_on_infrastructure(JavaClasses classes) {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..prompts.api.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..prompts.api.infrastructure..");
        rule.check(classes);
    }

    @ArchTest
    @DisplayName("Spring dependencies use constructor injection instead of field injection")
    static void no_field_injection(JavaClasses classes) {
        ArchRule rule = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .because("Constructor injection is required for architectural purity and testability");
        rule.check(classes);
    }

    @ArchTest
    @DisplayName("Presentation layer depends only on application, domain, Spring, Jakarta, and Java APIs")
    static void presentation_should_call_application_services(JavaClasses classes) {
        ArchRule rule = classes()
            .that().resideInAnyPackage("..prompts.api.presentation..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..prompts.api.application..",
                    "..prompts.api.domain..",
                    "org.springframework..",
                    "jakarta.servlet..",
                    "jakarta.validation..",
                    "java.."
            );
        rule.check(classes);
    }

    @ArchTest
    @DisplayName("Infrastructure layer implements application ports")
    static void infrastructure_implements_application_ports(JavaClasses classes) {
        ArchRule rule = classes()
            .that().resideInAnyPackage("..prompts.api.infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage("..prompts.api.application.port..");
        rule.check(classes);
    }
}
