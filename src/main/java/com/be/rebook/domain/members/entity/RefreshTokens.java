package com.be.rebook.domain.members.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class RefreshTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_id")
    private Long refreshId;

    private String username;
    private String refresh;
    private String expiration;

    public RefreshTokens(){}
    @Builder
    public RefreshTokens(String username, String refresh, String expiration){
        this.username = username;
        this.refresh = refresh;
        this.expiration = expiration;
    }
}
