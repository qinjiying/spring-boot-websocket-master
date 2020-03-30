package com.heiban.springboot.bean;

/**
 * 封装发给客服的消息
 */
public class Chat {

    String user; //用户名
    String content; //发送的内容

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
