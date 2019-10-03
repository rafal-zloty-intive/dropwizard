package com.example.helloworld.auth.jwt;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A set of extracted from JWT Authentication credentials.
 */
public class JwtCredentials {
    private final String sub;

    public JwtCredentials(String sub) {
        this.sub = requireNonNull(sub);
    }

    public String getSub() {
        return sub;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JwtCredentials other = (JwtCredentials) obj;
        return Objects.equals(sub, other.getSub());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sub);
    }

    @Override
    public String toString() {
        return String.format("JwtCredentials{sub=%s}", sub);
    }
}