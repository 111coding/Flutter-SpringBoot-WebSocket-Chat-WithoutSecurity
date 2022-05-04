package com.example.websocket;


import javax.security.auth.Subject;
import java.security.Principal;

public class ChatPrincipal implements Principal {

    private String name;

    public ChatPrincipal(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}