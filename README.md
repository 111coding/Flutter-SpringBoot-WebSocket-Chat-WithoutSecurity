# Flutter + SpringBoot Websocket Chat Example (Without Spring Security)
---

# Flutter Websocket Stomp Without Security

- dependencies
```
stomp_dart_client: ^0.4.4
```

### 1. Summary
#### 1.1. Connect
1) Backend에서 Spring Security를 적용하지 않아 url에 username 함께 날림
```'$serverAddr/ws?${senderController.text}'```

2) 소켓이 연결되면(```onConnect```) 자신에게 날라오는 메시지만 구독(```'/user/queue/pub'```)

```dart

stompClient = StompClient(
    /*
    * Node.js를 사용하면 Socket.io를 사용하는 것이 일반적이고 StompConfig()
    * -> ws://주소방식 ex) ws://192.168.0.5:8080
    * Spring을 사용한다면 SocketJS를 이용하는 것이 일반적이다. StompConfig.SockJS()
    * -> http://주소/ws 방식 ex) http://192.168.0.5:8080/ws
    */
    config: StompConfig.SockJS(
    // 1) Backend에서 Spring Security를 적용하지 않아 url에 username 함께 날림
    url: '$serverAddr/ws?${senderController.text}',
    webSocketConnectHeaders: {
        "transports": ["websocket"],
    },
    onConnect: (StompFrame frame) {
        setState(() {});
        print("연결시도");
        // 2) 웹소켓 연결 되면 구독하기!
        stompClient?.subscribe(
        destination: '/user/queue/pub',
        headers: {},
        callback: (frame) {
            // 구독 콜백
            msgArr.add(frame.body!);
            setState(() {});
        },
        );
    },
    beforeConnect: () async {
        print('waiting to connect...');
        await Future.delayed(const Duration(milliseconds: 200));
        print('connecting...');
    },
    onWebSocketError: (dynamic error) => print(error.toString()),
    //stompConnectHeaders: {'Authorization': 'Bearer yourToken'},
    //webSocketConnectHeaders: {'Authorization': 'Bearer yourToken'},
    ),
    );
    stompClient?.activate();

```

#### 1.2. Send Message

Backend에서 sender를 principal에서 받아와서 넣는 로직이 들어가서 receiver와 content(메시지 내용)만 날림

```dart
stompClient?.send(
    destination: '/app/chat.send',
    body: json.encode({"content": msgController.text, "receiver": receiverController.text}),
    headers: {},
);
```

---
# SpringBoot Websocket Without Security

- dependencies
```
implementation 'org.springframework.boot:spring-boot-starter-websocket'
```

### 1. Summary
1) ChatController
   - Chat Endpoint
2) ChatHandshakeHandler
   - Socket Handshake가 이루어 질 때 query string으로 받은 username으로 principal만들어서 주입!
3) ChatMessage
   - Chat DTO 
4) ChatPrincipal
   - username principal
5) WebSocketConfig
   - Websocket 관련 설정
   

### 2. Detail
1) WebSocketConfig
   - registerStompEndpoints : 웹소켓 연결 엔드포인트를 등록. Handshake가 일어날 때 principal를 주입시키기 위해 ChatHandshakeHandler를 등록한다 ( ```setHandshakeHandler(new ChatHandshakeHandler())``` ).
   - configureMessageBroker : 도착경로 prefix, 메시지 브로커 등 등록
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

   @Override
   public void configureMessageBroker(MessageBrokerRegistry config) {
      /*
       * setApplicationDestinationPrefixes
       * 도착 경로에 대한 prefix를 설정
       * /app : /topic/chat 이라는 구독을 신청했을 때 실제 경로는 /app/topic/chat
       */
      config.setApplicationDestinationPrefixes("/app");
      /*
       * enableSimpleBroker
       * 메시지 브로커 등록
       * 네이밍 : 보통 broadcast는 /topic, 특정 유저에게 보내는 것은 /queue
       */
      config.enableSimpleBroker("/queue");
      /*
       * setUserDestinationPrefix
       * 특정 유저에게 보낼 때(convertAndSendToUser) prefix
       * default : /user
       */
      // config.setUserDestinationPrefix("/user");
   }

   @Override
   public void registerStompEndpoints(StompEndpointRegistry registry) {
      /*
       * socket 연결 엔드포인트
       * 이번엔 ChatHandshakeHandler를 등록 해 ChatHandshakeHandler에서 principal 주입!
       */
      registry.addEndpoint("/ws")
              .setAllowedOriginPatterns("*")
              .setHandshakeHandler(new ChatHandshakeHandler())
              .withSockJS();
   }

}
```

2) ChatHandshakeHandler
   - ```WebSocketConfig```에서 등록한 socket handshake handler
   - 웹소켓 연결 요청에서 쿼리스트링에 함께 날라온 username으로 principal을 만들어서 주입 해준다.
```java
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
```

3) ChatController
   - 클라이언트의 요청 Endpoint
   - @SendToUser 대신 convertAndSendToUser 메서드 사용이유 : SendToUser는 security와 함께 사용!
   - 클라이언트가 웹소켓 연결 후 ```/chat.send```으로 메시지를 담아서 요청하면 principal에서 username을 가져와 sender에 담은 후 스트림을 구독중인 receiver에게 전달!
```java
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
```

