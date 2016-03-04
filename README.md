# Getting Started

_This assumes you have Docker installed and the Docker host IP is 192.168.99.100. If you use a different IP, consider find/replace across the entire repository._

Run `./setup.sh` to run and provision a Keycloak server using Docker. Then, start both the client and the resource application in their own terminal.

```bash
# Terminal 1
./setup.sh

# Terminal 2
cd client
./gradlew bootRun

# Terminal 3
cd resource
./gradlew bootRun
```

Next, open your browser to <http://localhost:8080/client>. You will be redirected to Keycloak where you can login using `user`/`user` or `admin`/`admin`. Next, select the `client.ExampleController` or navigate to it directly at <http://localhost:8080/client/example>.

The example controller uses `KeycloakRestTemplate` to request a resource from the `resource` application running on <http://localhost:8081/resource> which is secured using bearer-only authentication.

## Versions

- CentOS 7
- OpenJDK 8
- Keycloak 1.9.0.Final (standalone)
- Grails 3.1.3 (with Gradle 2.9)
- Spring Boot 1.3.3.RELEASE

# How it works

TODO


# The Client Application (Grails 3)

This section explains how to arrive at a working setup from scratch, assuming you have JDK 8 installed.

## Install Grails

Follow the instructions on [grails.org](http://www.grails.org) or, preferrably, use [sdkman](http://sdkman.io):

```bash
sdk install grails
```

## Create a Grails 3 application

```bash
grails create-app client
```

## Add Keycloak dependencies to build.gradle

Inside your `dependencies` block, add the following:

```gradle
compile "org.keycloak:keycloak-spring-security-adapter:1.9.0.Final"
runtime "org.jboss.logging:jboss-logging:3.3.0.Final"
```

The Keycloak adapter has transitive dependencies on Spring Security, so you don't need to include it explicitly. The runtime dependency on `jboss-logging` was not in the Keycloak documentation, but is required if you're not deploying to a JBoss/Wildfly server.

> I removed the Hibernate/GORM dependencies to make the client a bit more lightweight, but the point is that it's a stock Grails 3 application.

## Add keycloak.json

The `keycloak.json` file contains configuration data the adapter uses to communicate with Keycloak, such as the realm's public key (to verify JWT signatures), the root URL of the Keycloak server, the OAuth2 client-id to use, etc.

The Keycloak adapter automatically looks for it in `WEB-INF/keycloak.json`, which needs to be created inside `src/main/webapp/`. You can generate the contents of the file at the client's _Installation_ tab inside the Keycloak admin console; choose _Keycloak OIDC JSON_ format and double-check the value of `auth-server-url`.

```json
{
  "realm": "grails",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Q2QN4cCxc10Azb7CC4uas7efiPZOwwUHykxLIfHtc8Oxw2PkoBrSeMyA/skCewDvMJZHmc2SOdnA6DpqZ6rlXe7R6yzM4ytHIg4ijE4+PYotiP0lUTTTgIf3mbvaAiLUTZV1sz4TjmcSk95v4mb4CsqaDMGE+2EPyp0DW0NBqaynaE4aVKuGOxJAosJBBZodAOlthpMU59hL7JqBXQJKNvQsyxnJYHgJnLWaGUn8D8+Y1MMJgVjSrmtxqv/2coVaSJrRqSYirn+GmYgRGRR/BUyIikUROyGLo5Y/LtUvuTQM1vetHkcd3LJP7MpWqG9PDzvx412FbPw0sISzCLgaQIDAQAB",
  "auth-server-url": "http://192.168.99.100:8080/auth",
  "ssl-required": "external",
  "resource": "grails-client",
  "credentials": {
    "secret": "25d75d68-993b-4719-8380-dafc77566c95"
  }
}
```
## Configure Web Security

This is perhaps the most daunting piece of boilerplate, but bare with me. You can enable Spring Security by simply annotating a class with `@EnableWebSecurity`. This locks down your entire application with HTTP Basic Authentication using a random password generated during startup (written to stdout). However, we want the Keycloak adapter to act as an authentication provider, and we can extend the abstract `KeycloakWebSecurityConfigurerAdapter` to configure Spring Security to achieve this.

Create the file `grails-app/init/client/SecurityConfig.groovy`, and we'll add code to it step by step:

```groovy
package client

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.session.HttpSessionEventPublisher

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider())
    }
    
}
```

### Session Authentication Strategy

The [Keycloak documentation](https://keycloak.github.io/docs/userguide/keycloak-server/html/ch08.html) states:

> You must provide a session authentication strategy bean which should be of type `RegisterSessionAuthenticationStrategy` for public or confidential applications and `NullAuthenticatedSessionStrategy` for bearer-only applications.

```groovy
@Bean
@Override
protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl())
}
```

When choosing `RegisterSessionAuthenticationStrategy` you must also add a `HttpSessionEventPublisher` according to the Spring documentation:

> `HttpSessionEventPublisher` causes an `ApplicationEvent` to be published to the Spring `ApplicationContext` every time a `HttpSession` commences or terminates. This is critical, as it allows the `SessionRegistryImpl` to be notified when a session ends. Without it, a user will never be able to log back in again once they have exceeded their session allowance, even if they log out of another session or it times out.

```groovy
@Bean
ServletListenerRegistrationBean<HttpSessionEventPublisher> getHttpSessionEventPublisher() {
    new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher())
}
```

To learn more about Spring Security's session authentication strategies, check the [official documentation](https://docs.spring.io/spring-security/site/docs/current/reference/html/session-mgmt.html).

### HTTP Security

By default your application is not secured. If you want to lock down your entire application you need to do override the `configure` method:

```groovy
@Override
protected void configure(HttpSecurity http) throws Exception {
    super.configure http
    http
            .logout()
                .logoutSuccessUrl("/sso/login") // Override Keycloak's default '/'
            .and()
            .authorizeRequests()
                .antMatchers("/assets/*").permitAll()
                .anyRequest().hasAnyRole("USER", "ADMIN")
}
```

This does three things:

1. Change the logoutSuccess URL from `/` to the SSO login page, otherwise it prevents us from securing the home page
2. Allow anyone to access the `/assets` folder, otherwise your layout breaks on error pages
3. Require USER or ADMIN roles for anything else

If you want to secure only specific paths, you can do this:

```groovy
@Override
protected void configure(HttpSecurity http) throws Exception {
    super.configure http
    http
            .authorizeRequests()
                .antMatchers("/admin/*").hasRole("ADMIN")
                .antMatchers("/app/*").hasAnyRole("USER", "ADMIN")
                .anyRequest().permitAll()
}
```

This leaves the home page and assets folder unsecured, but requires the ADMIN role to access `/admin/` for example.

### Client to Client support

The [Keycloak documentation](https://keycloak.github.io/docs/userguide/keycloak-server/html/ch08.html) states:
> To simplify communication between clients, Keycloak provides an extension of Spring's `RestTemplate` that handles bearer-token authentication for you. To enable this feature your security configuration must add the `KeycloakRestTemplate` bean. The bean must be scoped as a prototype to function correctly.

```groovy
@Autowired
public KeycloakClientRequestFactory keycloakClientRequestFactory

@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public KeycloakRestTemplate keycloakRestTemplate() {
    return new KeycloakRestTemplate(keycloakClientRequestFactory)
}
```

You can inject this bean and use it to transparently access a protected resource without dealing with access tokens, for example:

```groovy
class RemoteStuffService {
    
    private static final STUFF_ENDPOINT = "http://server/api/stuff"
    KeycloakRestTemplate keycloakRestTemplate
    
    JsonNode getStuff() {
        keycloakRestTemplate.getForObject(STUFF_ENDPOINT, JsonNode.class)
    }
}
```

> As a sidenote, you can access the token and other interesting things related to the authentication via `org.springframework.security.core.context.SecurityContextHolder.context.authentication`

### Allow SecurityConfig to be detected by Spring

Make a small change to `grails-app/init/client/Application.groovy` to include the `client` package in scanning for Spring components such as `@Configuration` classes. Since it's short, I've included the entire class below:

```groovy
package client

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = "client")
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
```

# The Resource Application (Spring Boot)

TODO