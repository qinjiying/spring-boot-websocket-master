package com.heiban.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.heiban.springboot.components.MyHandshakeInterceptor;
import com.heiban.springboot.components.MyWebSocketHandler;

@Configuration
@EnableWebSocket //开启WebSocket服务来接收请求
public class WebSocketConfig implements WebSocketConfigurer {

    //注入WebSocket处理器
    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    //注入WebSocket拦截器
    @Autowired
    private MyHandshakeInterceptor myHandshakeInterceptor;

    //注册服务
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {

        //addHandler第一个参数是WebSocket处理器  第二个参数是访问地址 前端的请求地址是ws://127.0.0.1/websocket  addInterceptors添加拦截器
        webSocketHandlerRegistry.addHandler(myWebSocketHandler,"/websocket").addInterceptors(myHandshakeInterceptor);

        //如果客户端的浏览器不支持WebSocket 就启用备用选项sockjs  前端的请求地址是http://127.0.0.1/websocketjs
        //添加.withSockJS();即可支持sockjs
        webSocketHandlerRegistry.addHandler(myWebSocketHandler,"/websocketjs").addInterceptors(myHandshakeInterceptor).withSockJS();
    }

}
