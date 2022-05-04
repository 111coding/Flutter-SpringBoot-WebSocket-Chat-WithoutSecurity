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