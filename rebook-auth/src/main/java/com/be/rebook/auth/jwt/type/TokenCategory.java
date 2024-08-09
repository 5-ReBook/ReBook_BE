package com.be.rebook.auth.jwt.type;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum TokenCategory {
    ACCESS("access"),
    REFRESH("refresh");

    private final String name;
    private Long expiry;

    TokenCategory(String name) {
        this.name = name;
    }

    @JsonCreator
    public static TokenCategory getEnumFromValue(String value) {
        for (TokenCategory e : values()) {
            if (e.name.equals(value)) {
                return e;
            } else if (e.name.toUpperCase(Locale.ROOT).equals(value.toUpperCase(Locale.ROOT))) {
                return e;
            }
        }
        return null;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }
}