import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:convert';

import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

const serverAddr = "http://localhost:8080";

class ChatPage extends StatefulWidget {
  const ChatPage({Key? key}) : super(key: key);

  @override
  _ChatPageState createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final senderController = TextEditingController();
  final receiverController = TextEditingController();
  final msgController = TextEditingController();

  StompClient? stompClient;

  final msgArr = <String>[];

  @override
  void dispose() {
    senderController.dispose();
    receiverController.dispose();
    msgController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Expanded(
              child: ListView.builder(
                itemCount: msgArr.length,
                itemBuilder: ((context, index) => Text(msgArr[index])),
              ),
            ),
            if (stompClient == null) ...[
              _field("sender", senderController),
              eHeight(20),
              ElevatedButton(
                onPressed: _connect,
                child: const Text("connect"),
              ),
            ],
            if (stompClient != null) ...[
              eHeight(20),
              Text("sender : ${senderController.text}"),
              _field("receiver", receiverController),
              _field("message", msgController),
              eHeight(20),
              ElevatedButton(
                onPressed: _send,
                child: const Text("메시지 전송"),
              ),
            ],
            eHeight(20),
          ],
        ),
      ),
    );
  }

  void _send() {
    stompClient?.send(
      destination: '/app/chat.send',
      body: json.encode({"content": msgController.text, "receiver": receiverController.text}),
      headers: {},
    );
  }

  void _connect() {
    stompClient = StompClient(
      /*
       * Node.js를 사용하면 Socket.io를 사용하는 것이 일반적이고 StompConfig()
       * -> ws://주소방식 ex) ws://192.168.0.5:8080
       * Spring을 사용한다면 SocketJS를 이용하는 것이 일반적이다. StompConfig.SockJS()
       * -> http://주소/ws 방식 ex) http://192.168.0.5:8080/ws
       */
      config: StompConfig.SockJS(
        url: '$serverAddr/ws?${senderController.text}',
        webSocketConnectHeaders: {
          "transports": ["websocket"],
        },
        onConnect: (StompFrame frame) {
          setState(() {});
          print("연결시도");
          // 웹소켓 연결 되면 구독하기!
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
  }

  Widget eHeight(double height) => SizedBox(height: height);

  Widget _field(String lable, TextEditingController controller) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20),
      constraints: const BoxConstraints(maxWidth: 500),
      width: double.infinity,
      height: 50,
      child: Row(
        children: [
          SizedBox(
            width: 200,
            child: Text(lable),
          ),
          Expanded(
            child: TextField(
              controller: controller,
            ),
          ),
        ],
      ),
    );
  }
}
