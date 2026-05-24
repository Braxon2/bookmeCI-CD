package com.dusanbranovic.bookme.models;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum UserType implements GrantedAuthority {
    ADMIN, USER, OWNER;

    @Override
    public @Nullable String getAuthority() {
        return name();
    }
}
