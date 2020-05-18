# spring-boot-websocket-master
WebSocket简介：
WebSocket是一种在单个TCP连接上进行全双工通信的协议。WebSocket使得客户端和服务器之间的数据交换变得更加简单，允许服务端主动向客户端推送数据。在WebSocket API中，浏览器和服务器只需要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。
如果不用WebSocket我们也可以使用Ajax轮询，或定时刷新页面。但是这是很耗费服务器资源的。如果是需要低延迟的项目下就应该使用WebSocket，比如本次介绍的项目“网页客服与多用户聊天”这种需要低延迟的。

更直接的说明：
当听到Socket大家应该不会陌生，Java网络编程的时候，我们用的就是用Socket，回忆一下网络编程，代码如下图：





在使用方法上基本也是异曲同工，如果没有写过Java网络编程也没关系，看完这篇文章就可以掌握WebSocket了。



3、利用IDEA创建项目

利用IDEA创建项目并导入相关依赖：





除了上利用IDEA创建项目导入的依赖，还需要导入JSON依赖，用于通信这样比较方便。在pom.xml文件中增加以下依赖：
<dependency>
    <groupId>net.sf.json-lib</groupId>
    <artifactId>json-lib</artifactId>
    <version>2.2.3</version>
    <classifier>jdk15</classifier>
</dependency>



4、SpringBoot整合WebSocket原理

特意说明一下：spring实现WebSocket的方式有多种，我介绍的是其中一种，这种方法我感觉最灵活。

1、WebSocket处理器创建一个名为MyWebSocketHandler的类并实现WebSocketHandler接口，代码如下（不包含具体实现的代码）：
/**
 * WebSocket处理器
 * @author 公众号：鲁智深菜园子
 * @date 2020/2/23
 */
@Component
public class MyWebSocketHandler implements WebSocketHandler {
    //存储所有已经连接的用户
    public static ConcurrentHashMap<String,WebSocketSession> concurrentHashMap = new ConcurrentHashMap<String,WebSocketSession>();

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {}
    
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {}

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {}

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {}

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
WebSocketHandler接口的方法官网给了具体的介绍，如下图：
官方文档网址：
https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/socket/WebSocketHandler.html



这些方法都是一些回调方法，当发生相应的状态的时候就会自动调用相应的方法。在空间上理解，每个用户在连接成功后都会创建一个处理器对象。

处理器的基本用法是：
当用户连接成功后就在afterConnectionEstablished方法中将用户的ID与WebSocketSession存储到类的ConcurrentHashMap中。当需要发送消息给某个用户的时候就可以通过用户ID从ConcurrentHashMap中获取用户的WebSocketSession。

通过WebSocketSession发送消息给用户的代码如下:
webSocketSession.sendMessage(new TextMessage("要发送的内容"));


用ConcurrentHashMap存储所有用户的好处是：
线程安全而且用了分离锁实现多个线程间的并发写操作。



2、WebSocket拦截器创建一个名为MyHandshakeInterceptor的类并实现HandshakeInterceptor接口，具体代码如下：


//WebSocket拦截器
@Component
public class MyHandshakeInterceptor implements HandshakeInterceptor {
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

上面给出的代码是该项目的具体代码，因为这个比较重要，HttpSession与WebSocketSession连接的桥梁就在这里了。

根据上面的代码中，以下这两条语句就是获取HttpSession的，他的作用就是判断用户是否登录或标识用户。
ServletServerHttpRequest servletRequset = (ServletServerHttpRequest) serverHttpRequest;
HttpSession httpSession = servletRequset.getServletRequest().getSession(false);

根据上面的代码中，以下这map.put()一条语句就是把用户的标识存储到WebSocketSession中了。
map.put("user",httpSession.getAttribute("user"));
那么如何在MyWebSocketHandler处理器中拿到这一条标识呢？代码如下：
webSocketSession.getAttributes().get("user").toString();

拦截器中握手之前执行的方法是：
public boolean beforeHandshake()

拦截器中握手之后执行的方法是：
public void afterHandshake()


3、注册WebSocket服务创建一个名为WebSocketConfig的类并实现WebSocketConfigurer接口，具体代码如下：

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
注册WebSocket服务的时候就需要用得到第一步与第二步中创建的处理器与拦截器了。具体的用法，上面的代码注释中都详细的说明了。
在该类的头部需要用@EnableWebSocket 来开启WebSocket服务来接收请求。


4、前端创建WebSocket对象与回调处理的JS代码，如下：
<script type="text/javascript">
  var socket = null;

  //适配不同的浏览器 创建Socket对象
  if('WebSocket' in window){
    socket = new WebSocket("ws://127.0.0.1/websocket");
    console.log("浏览器内置的WebSocket");
  }else if('MozWebSocket' in window){
    //火狐浏览器
    socket = new MozWebSocket("ws://127.0.0.1/websocket");
    console.log("火狐浏览器内置的WebSocket");
  }else{
    //当浏览器不支持WebSocket 采用sockjs
    socket = new SockJS("http://127.0.0.1/websocketjs");
    console.log("采用sockjs");
  }

  //连接成功后的回调方法
  socket.onopen = function(){
  }

  //接收消息的时候触发
  socket.onmessage = function(data){
    console.log(data.data);//获取接收到的数据
    var parse = JSON.parse(data.data);//将字符串解释成JSON对象
  }

  //连接发生错误时的回调方法
  socket.onerror = function(){
  }

  //某一方发生关闭的时候
  socket.onclose = function(){
  }
</script>

介绍一下前端创建WebSocket对象
socket = new WebSocket("ws://127.0.0.1/websocket");
这个代码中因为WebSocket是ws协议，所以需要以ws://开头。127.0.0.1代表服务器地址，其中的/websocket就是我们第三步中注册WebSocket服务映射的地址。

为了保证确保浏览器的兼容，我们还需要导入sockjs.min.js 当浏览器不支持WebSocket 采用sockjs 。
socket = new SockJS("http://127.0.0.1/websocketjs");
其中的/websocketjs就是我们第三步中注册WebSocket服务映射的地址。
SockJS的一大好处在于提供了浏览器兼容性。优先使用原生WebSocket，如果在不支持websocket的浏览器中，会自动降为轮询的方式。

前端的WebSocket有几个回调方法：
连接成功后的回调方法：
 socket.onopen = function(){}
接收到消息的回调方法 ：
socket.onmessage= function(data){} 可以通过data.data获取接收到的文字数据。
某一方发生关闭时的回调方法： 
socket.onclose= function(){}
连接发生错误时的回调方法：
 socket.onerror= function(){}
发送消息到服务器的方法是：
socket.send("发送的内容");//发送到服务器




5、WebSocket运行流程


1、访问控制器 http://127.0.0.1/ 来到了首页。
2、前端new WebSocket("ws://127.0.0.1/websocket")；后就来到了后端的拦截器的beforeHandshake方法中通过ServerHttpRequest转子类后获取HttpSession后，提取标识保存在 WebSocketSession中，这样就能标识是哪个用户了。
3、后端拦截器中的beforeHandshake的方法返回true后，就来到了 处理器的afterConnectionEstablished方法中，将WebSocketSession存储到ConcurrentHashMap中以便随时发送消息。


项目目录结构简介图如下：




如果你想搭建该项目只需要直接运行即可，访问的链接如下：
用户聊天的页面的链接是：http://127.0.0.1/  
客服聊天的页面的链接是：http://127.0.0.1/chat.html
