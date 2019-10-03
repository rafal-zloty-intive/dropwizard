package com.example.helloworld.auth;

import com.example.helloworld.auth.jwt.JwtCredentials;
import com.example.helloworld.core.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class ExampleAuthenticator implements Authenticator<JwtCredentials, User> {
    /**
     * Valid users with mapping user -> roles
     */
    private static final Map<String, Set<String>> VALID_USERS;

    static {
        final Map<String, Set<String>> validUsers = new HashMap<>();
        validUsers.put("guest", Collections.emptySet());
        validUsers.put("Administrator", Collections.singleton("ADMIN"));
        VALID_USERS = Collections.unmodifiableMap(validUsers);
    }

    @Override
    public Optional<User> authenticate(JwtCredentials credentials) throws AuthenticationException {
        final String username = credentials.getSub();
        if (username != null && VALID_USERS.containsKey(username)) {
            return Optional.of(new User(username, VALID_USERS.get(username)));
        }
        return Optional.empty();
    }
}
