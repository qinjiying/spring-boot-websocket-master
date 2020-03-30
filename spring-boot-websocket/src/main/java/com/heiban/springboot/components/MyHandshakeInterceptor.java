package com.heiban.springboot.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

//WebSocket拦截器
@Component
public class MyHandshakeInterceptor implements HandshakeInterceptor {

    Logger logger = LoggerFactory.getLogger(getClass());//记录器

    //握手之前调用
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {

        ServletServerHttpRequest servletRequset = (ServletServerHttpRequest) serverHttpRequest;

        //这里从request中获取session,获取不到不创建
        HttpSession httpSession = servletRequset.getServletRequest().getSession(false);

        if (httpSession.getAttribute("user") != null){
            map.put("user",httpSession.getAttribute("user"));
            return true;
        }

        if (httpSession.getAttribute("chat") != null){
            logger.info("客服登录成功");
            map.put("chat",httpSession.getAttribute("chat"));
            return true;
        }


        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }


}
