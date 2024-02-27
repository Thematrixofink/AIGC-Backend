package com.ink.backend.manager;

import com.ink.backend.common.ErrorCode;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    private static ConcurrentHashMap<Session, WebSocketServer> websocketMap = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private Session id;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        this.id = session;
        websocketMap.put(session, this);

        try {
            sendMessage("当前有连接:"+session.getId()+"进入,连接数一共:" + getOnlineCount());
            System.out.println(session);
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        subOnlineCount();           //在线数减1
        webSocketSet.remove(this);  //从set中删除
        websocketMap.remove(session);
        System.out.println("有一连接关闭！当前在线连接为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        try {
            sendMessageToUser(message, session);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        new BusinessException(ErrorCode.SYSTEM_ERROR,"WebSocket错误");
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    public void sendMessageToUser(String message, Session session) throws IOException {
        if (websocketMap.get(session) != null) {
            if(!id.equals(session)) {
                websocketMap.get(session).sendMessage("用户" + session.getId() + "发来消息：" + message);
            } else {
                websocketMap.get(session).sendMessage("用户" + session.getId() + "发来消息：" + message);
            }
        } else {
            //如果用户不在线则返回不在线信息给自己
            sendMessageToUser("当前用户不在线",id);
        }
    }

    /**
     * 群发消息
     * @param message
     */
    public void sendMessageToAll(String message) {
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message) throws IOException {
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
