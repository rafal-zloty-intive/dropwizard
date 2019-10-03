package com.example.helloworld.resources;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.auth.jwt.JwtCredentialAuthFilter;
import com.example.helloworld.core.User;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Base64;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ProtectedResourceTest {
    private static final String PREFIX = "Bearer";
    private static final String PROPER_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBZG1pbmlzdHJhdG9yIiwibmFtZSI6IkpvaG4gU21pdGgiLCJpYXQiOjE1MTYyMzkwMjJ9.5gap4WNtbvFZqPugl846mBZA-7oKrV1GQ3xbcFCcHvw";

    private static final JwtCredentialAuthFilter<User> JWT_AUTH_HANDLER =
            new JwtCredentialAuthFilter.Builder<User>()
                    .setJwtSecret(Base64.getDecoder().decode("cXdlcnR5"))
                    .setAuthenticator(new ExampleAuthenticator())
                    .setAuthorizer(new ExampleAuthorizer())
                    .setPrefix(PREFIX)
                    .setRealm("SUPER SECRET STUFF")
                    .buildAuthFilter();

    public static final ResourceExtension RULE = ResourceExtension.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(JWT_AUTH_HANDLER))
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(ProtectedResource.class)
            .build();

    @Test
    public void testProtectedEndpoint() {
        String secret = RULE.target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, PREFIX + " " + PROPER_JWT_TOKEN)
                .get(String.class);
        assertThat(secret).startsWith("Hey there, Administrator. You know the secret!");
    }

    @Test
    public void testProtectedEndpointNoCredentials401() {
        try {
            RULE.target("/protected").request()
                .get(String.class);
            failBecauseExceptionWasNotThrown(NotAuthorizedException.class);
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly(PREFIX + " realm=\"SUPER SECRET STUFF\"");
        }
    }

    @Test
    public void testProtectedEndpointPrefixWrong401() {
        try {
            RULE.target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + JWT_AUTH_HANDLER)
                .get(String.class);
            failBecauseExceptionWasNotThrown(NotAuthorizedException.class);
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }
    }

    @Test
    public void testProtectedEndpointBadSecret401() {
        try {
            RULE.target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, PREFIX + " eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBZG1pbmlzdHJhdG9yIiwibmFtZSI6IkpvaG4gU21pdGgiLCJpYXQiOjE1MTYyMzkwMjJ9.SRipfSXZgzpAeGTrvqzwOLFLyRfveUDsPCJfQST66w4")
                .get(String.class);
            failBecauseExceptionWasNotThrown(NotAuthorizedException.class);
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                .containsOnly(PREFIX + " realm=\"SUPER SECRET STUFF\"");
        }
    }

    @Test
    public void testProtectedAdminEndpoint() {
        String secret = RULE.target("/protected/admin").request()
                .header(HttpHeaders.AUTHORIZATION, PREFIX + " " + PROPER_JWT_TOKEN)
                .get(String.class);
        assertThat(secret).startsWith("Hey there, Administrator. It looks like you are an admin.");
    }

    @Test
    public void testProtectedAdminEndpointPrincipalIsNotAuthorized403() {
        try {
            RULE.target("/protected/admin").request()
                    .header(HttpHeaders.AUTHORIZATION, PREFIX + " eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJndWVzdCIsIm5hbWUiOiJKb2huIFNtaXRoIiwiaWF0IjoxNTE2MjM5MDIyfQ.eMm0Hh0GItuOqmf9X8DMmL4WL8QH4MgbZErpYlz3SyA")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }
}
