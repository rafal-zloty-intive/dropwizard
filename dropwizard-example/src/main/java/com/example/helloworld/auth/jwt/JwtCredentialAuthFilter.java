package com.example.helloworld.auth.jwt;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import io.dropwizard.auth.AuthFilter;

import static java.util.Objects.requireNonNull;

@Priority(Priorities.AUTHENTICATION)
public class JwtCredentialAuthFilter<P extends Principal> extends AuthFilter<JwtCredentials, P> {
    private static final String JWT_AUTHETICATION_SCHEME = "JWT";
    private static final String CLAIM_NAME_SUB = "sub";

    private final byte[] jwtSecret;

    private JwtCredentialAuthFilter(byte[] jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String authorizationToken = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final JwtCredentials credentials = getCredentials(authorizationToken);
        if (!authenticate(requestContext, credentials, JWT_AUTHETICATION_SCHEME)) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }
    }

    /**
     * Parses a Base64-encoded JWT of the `Authorization` header in the form of
     * `Bearer
     * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBZG1pbmlzdHJhdG9yIn0.QsvPyc0Rg71GisxBV-kMPn3RTp8CSXBr2WHIMWWCRJA`.
     * Check if JWT is proper using the the HMACSHA256 algorithm.
     *
     * @param header the value of the `Authorization` header or null if the header
     *               is incorrect
     * @return a sub as {@link JwtCredentials}
     */
    @Nullable
    private JwtCredentials getCredentials(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }

        final int space = authorizationHeader.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = authorizationHeader.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            return null;
        }

        try {
            final String jwtToken = authorizationHeader.substring(space + 1);
            final Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            final JWTVerifier verifier = JWT.require(algorithm).build();

            final DecodedJWT jwt = verifier.verify(jwtToken);

            final Map<String, Claim> claims = jwt.getClaims();
            final Claim claim = claims.get(CLAIM_NAME_SUB);
            final String sub = claim != null ? claim.asString() : null;
            return new JwtCredentials(sub);

        } catch (JWTCreationException | SignatureVerificationException e) {
            logger.warn("Error verifying credentials", e);
            return null;
        }
    }

    public static class Builder<P extends Principal>
            extends AuthFilterBuilder<JwtCredentials, P, JwtCredentialAuthFilter<P>> {

        private byte[] jwtSecret;

        public Builder<P> setJwtSecret(byte[] jwtSecret) {
            this.jwtSecret = jwtSecret;
            return this;
        }

        @Override
        protected JwtCredentialAuthFilter<P> newInstance() {
            requireNonNull(jwtSecret, "JWT secret is not set");
            return new JwtCredentialAuthFilter<>(jwtSecret);
        }
    }
}