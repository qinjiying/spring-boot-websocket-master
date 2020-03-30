package com.heiban.springboot.components;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.heiban.springboot.bean.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

//WebSocket处理器
@Component
public class MyWebSocketHandler implements WebSocketHandler {

	// 存储所有已经连接的用户
	public static ConcurrentHashMap<String, WebSocketSession> concurrentHashMap = new ConcurrentHashMap<String, WebSocketSession>();

	Logger logger = LoggerFactory.getLogger(getClass());// 记录器

	// 在WebSocket协商成功且WebSocket连接已打开并可以使用后调用。
	@Override
	public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {

		if (webSocketSession.getAttributes().get("chat") == null) {
			webSocketSession.sendMessage(new TextMessage("系统提示：你好你已经连接上啦!"));
			concurrentHashMap.put(webSocketSession.getAttributes().get("user").toString(), webSocketSession);
			logger.info("用户" + webSocketSession.getAttributes().get("user") + "已连接");

			// 提示客服有用户上线
			WebSocketSession socketSession = concurrentHashMap.get("chat");
			if (socketSession != null) {// 如果客服上线了
				Chat chat = new Chat();
				chat.setUser(webSocketSession.getAttributes().get("user").toString());// 设置用户的名称
				chat.setContent("用户上线了");// 设置用户发送的内容
				socketSession.sendMessage(new TextMessage(JSON.toJSONString(chat)));// 将信息发送给客服
			}

		} else {
			concurrentHashMap.put("chat", webSocketSession);
			logger.info("客服已连接");
		}

		if (concurrentHashMap.get("chat") == null) {
			webSocketSession.sendMessage(new TextMessage("系统提示：客服还没有上线"));
		}

		Iterator it = concurrentHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			logger.info("在线session：" + key);
		}

	}

	// 当新的WebSocket消息到达时调用。
	@Override
	public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage)
			throws Exception {

		if (webSocketSession.getAttributes().get("chat") == null) {// 不是客服的消息

			Chat chat = new Chat();
			chat.setUser(webSocketSession.getAttributes().get("user").toString());// 设置用户的名称
			chat.setContent(webSocketMessage.getPayload().toString());// 设置用户发送的内容

			logger.info("用户发送给客服的消息是" + chat.getContent());

			WebSocketSession socketSession = concurrentHashMap.get("chat");
			if (socketSession != null) {// 如果客服上线了
				socketSession.sendMessage(new TextMessage(JSON.toJSONString(chat)));// 将信息发送给客服
			}

		} else {
			if (checkVildJson(webSocketMessage.getPayload().toString())) {
				// 客服发送的消息
				// 将JSON数据解释成JAVA对象
				Chat chat = JSON.parseObject(webSocketMessage.getPayload().toString(), Chat.class);
				WebSocketSession socketSession = concurrentHashMap.get(chat.getUser());// 通过用户名获取WebSocketSession

				logger.info("客服发送给用户" + chat.getUser() + "的消息是" + chat.getContent());
				socketSession.sendMessage(new TextMessage(chat.getContent()));// 将客服回复的内容发送给用户
			}
		}

	}

	public static final boolean checkVildJson(String srt) {
		try {
			JSONObject.parseObject(srt);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}

	// 处理来自基础WebSocket消息传输的错误。
	@Override
	public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
		// 发生异常关闭WebSocketSession
		if (webSocketSession.isOpen()) {
			webSocketSession.close();
		}
		logger.info(webSocketSession.getAttributes().get("user") + "发生错误 退出连接");
		concurrentHashMap.remove(webSocketSession.getId());
	}

	// 在任一侧关闭WebSocket连接之后或发生传输错误之后调用。
	@Override
	public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
		logger.info(webSocketSession.getAttributes().get("user") + "退出连接");
		concurrentHashMap.remove(webSocketSession.getId());
	}

	// WebSocketHandler是否处理部分消息。
	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}
