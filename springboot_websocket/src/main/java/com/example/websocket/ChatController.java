package com.example.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/chat.send") // 요청 엔드포인트 /app/chat.send
    @SendTo("/queue/pub")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        /*
         * 특정 유저에게만 보내기!
         * 구독할 때는 /user/topic/pub
         */
        message.setSender(principal.getName());
        logger.info("Sender : " + message.getSender() + ", Receiver : " + message.getReceiver() + ", Content : "+message.getContent());
        messageTemplate.convertAndSendToUser(message.getReceiver(), "/queue/pub", message);
    }

}