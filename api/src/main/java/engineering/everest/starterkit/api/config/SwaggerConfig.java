package engineering.everest.starterkit.api.config;

import com.google.common.base.Predicate;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Profile("!prod")
@Configuration
@EnableSwagger2
@ComponentScan({"engineering.everest.starterkit.api"})
@PropertySource("swagger.properties")
@SwaggerDefinition(tags = {
        @Tag(name = "Admin", description = "Internal administrative use only"),
        @Tag(name = "System", description = "Information on the system you are interacting with"),
        @Tag(name = "Organizations", description = "Organization information and management"),
        @Tag(name = "Projects", description = "Project information and management"),
        @Tag(name = "Users", description = "User information and management"),
        @Tag(name = "Assets", description = "Asset information and management"),
        @Tag(name = "AssetTypes", description = "Asset Type information and management"),
        @Tag(name = "HelpSessions", description = "Calling, responding and managing help sessions"),
        @Tag(name = "OrgAdmins", description = "Organization Admin information and management"),
        @Tag(name = "Experts", description = "Organization Expert information and management"),
        @Tag(name = "OrganizationBoundNetworks", description = "Organization Bound Network information and management"),
})
public class SwaggerConfig {

    @Bean
    public Docket apiDocumentation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("internal")
                .apiInfo(internalApiInfo())
                .select()
                .paths(pathsToDocument())
                .build();
    }

    @SuppressWarnings("Guava")
    private Predicate<String> pathsToDocument() {
        //noinspection unchecked
        return or(
                regex("/api/.*"),
                regex("/oauth/.*"),
                regex("/tokens/.*")
        );
    }

    private ApiInfo internalApiInfo() {
        return new ApiInfoBuilder()
                .title("REST API")
                .description("Our REST API. Subject to change without notice.")
                .termsOfServiceUrl("http://my.project.web")
                .version("0.0")
                .build();
    }
}
