package app.prompts;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "app.prompts", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureComplianceTest {

    @ArchTest
    static final ArchRule domain_should_be_framework_free = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "..infrastructure.."
            );

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure = noClasses()
            .that().resideInAnyPackage("..prompts.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..prompts.infrastructure..");

    @ArchTest
    static final ArchRule no_field_injection = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .because("Constructor injection is required for architectural purity and testability");

    @ArchTest
    static final ArchRule presentation_should_call_application_services = classes()
            .that().resideInAnyPackage("..prompts.presentation..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..prompts.application..",
                    "..prompts.domain..",
                    "org.springframework..",
                    "jakarta.servlet..",
                    "jakarta.validation..",
                    "java.."
            );

    @ArchTest
    static final ArchRule infrastructure_implements_application_ports = classes()
            .that().resideInAnyPackage("..prompts.infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage("..prompts.application.port..");
}
