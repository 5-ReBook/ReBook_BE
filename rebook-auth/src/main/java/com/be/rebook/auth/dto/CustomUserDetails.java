package com.be.rebook.auth.dto;

import com.be.rebook.auth.entity.Members;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final Members members;

    public CustomUserDetails(Members members) {
        this.members = members;
    }

    public String getRoleName() {
        return members.getRole();
    }

    // 역할 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(members::getRole);
        return collection;
    }

    @Override
    public String getPassword() {
        return members.getPassword();
    }

    @Override
    public String getUsername() {
        return members.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
