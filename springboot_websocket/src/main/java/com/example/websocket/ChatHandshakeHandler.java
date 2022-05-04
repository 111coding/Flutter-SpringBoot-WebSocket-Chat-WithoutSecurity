package com.example.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/*
 *  Socket Handshake가 이루어 질 때 query string으로 받은 username으로 principal만들어서 주입!
 * ex) http://localhost/ws?test => username : test
 */
public class ChatHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String username = request.getURI().getQuery();
        return new ChatPrincipal(username);
    }
}