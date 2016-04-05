package resource;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.io.InputStream;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    protected static class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(keycloakAuthenticationProvider());
        }

        /**
         * Overrides default keycloak config resolver behaviour (/WEB-INF/keycloak.json) by a simple mechanism.
         * <p>
         * This example loads other-keycloak.json when the parameter use.other is set to true, e.g.:
         * {@code ./gradlew bootRun -Duse.other=true}
         *
         * @return keycloak config resolver
         */
        @Bean
        public KeycloakConfigResolver keycloakConfigResolver() {
            return new KeycloakConfigResolver() {

                private KeycloakDeployment keycloakDeployment;

                @Override
                public KeycloakDeployment resolve(HttpFacade.Request facade) {
                    if (keycloakDeployment != null) {
                        return keycloakDeployment;
                    }

                    boolean useOther = System.getProperty("use.other", "false").equals("true");
                    String path = useOther ? "/other-keycloak.json" : "/keycloak.json";
                    InputStream configInputStream = getClass().getResourceAsStream(path);

                    if (configInputStream == null) {
                        throw new RuntimeException("Could not load Keycloak deployment info: " + path);
                    } else {
                        keycloakDeployment = KeycloakDeploymentBuilder.build(configInputStream);
                    }

                    return keycloakDeployment;
                }
            };
        }

        @Bean
        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            return new NullAuthenticatedSessionStrategy();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests().anyRequest().authenticated();
        }
    }
}