package org.twins.core.config;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import com.pupay.api.config.SessionConfig;
import com.pupay.api.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.AntPathMatcher;
import springfox.documentation.OperationNameGenerator;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.money.CurrencyUnit;
import java.util.List;

import static com.esas.common.Lang.newList;

@Profile(value = {"!release"})
@Configuration
@EnableSwagger2
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ComponentScan(basePackageClasses = com.pupay.api.swagger.SwaggerConfig.class, excludeFilters = @Filter({Configuration.class}))
public class SwaggerConfig {
    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket swaggerSettings() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .securitySchemes(newList(new ApiKey(SessionConfig.AUTH_HEADER, SessionConfig.AUTH_HEADER, "header")))
                .securityContexts(newList(securityContext()))
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .alternateTypeRules(AlternateTypeRules.newRule(
                        typeResolver.resolve(List.class, CurrencyUnit.class),
                        typeResolver.resolve(List.class, String.class),
                        Ordered.HIGHEST_PRECEDENCE),
                        AlternateTypeRules.newRule(
                                CurrencyUnit.class, String.class, Ordered.HIGHEST_PRECEDENCE)
                )
                .apiInfo(apiInfo("1.0")).select().paths(path -> !"/error".equals(path)
                        && !path.startsWith("/callback")
                        && !path.startsWith("/shutdown"))
                .paths(Predicates.not(PathSelectors.regex("/private/alfabank/3d/callback")))
                .build();

    }

    private ApiInfo apiInfo(String version) {
        return new ApiInfo("Twins API", // title
                "", // description
                version, // version
                null, // termsOfServiceUrl
                (Contact) null, // contact
                null, // license
                null, // licenseUrl
                newList());
    }

    private SecurityContext securityContext() {
        AntPathMatcher matcher = new AntPathMatcher();
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(s -> !matcher.match(SecurityConfig.API_URL + "/auth/verify", s)
                        && !matcher.match(SecurityConfig.API_URL + "/auth/login", s)
                        && !matcher.match(SecurityConfig.API_URL + "/callback/**", s)
                        && !matcher.match(SecurityConfig.API_URL + "/web/**", s))
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newList(
                new SecurityReference(SessionConfig.AUTH_HEADER, authorizationScopes));
    }

    @Bean
    @Primary
    public OperationNameGenerator getSameNameGenerator() {
        return new OperationNameGenerator() {
            @Override
            public String startingWith(String prefix) {
                return prefix;
            }
        };
    }


}
