package com.heiban.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Map;


@Controller
public class Index {


    //首页渲染
    @RequestMapping({"/","/index.html"})
    public String index(HttpSession session){
        if(session.getAttribute("user")==null){
            session.setAttribute("user",new SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()));
        }

        return "index";
    }


    //登录页面渲染
    @RequestMapping("/login.html")
    public String login(HttpSession session){

        return "login";
    }


    //客服页面渲染
    @RequestMapping("/chat.html")
    public String chat(HttpSession session){
        if(session.getAttribute("chat")==null){
            return "redirect:/login.html";
        }
        return "chat";
    }


    //模拟客服登录方法
    @PostMapping("/loginmethod")
    public String loginmethod(HttpServletRequest request, Map<String,Object> map, HttpSession session){

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if(username.equals("admin")&&password.equals("123456")){
            session.setAttribute("chat","1");
            return "redirect:/chat.html";//重定向
        }else{
            map.put("msg","用户名或密码错误！");
            return "login";
        }

    }



}
