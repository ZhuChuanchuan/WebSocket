import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create by ZHCC on 2018/9/17
 */

@ServerEndpoint(value="/websocket/chat")
public class ChatEntpoint {

    private static final String GUEST_PREFIX = "访客";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);

    //定义一个集合，用于保存所有接入的websocket客户端
    private static final Set<ChatEntpoint> clientSet = new CopyOnWriteArraySet<>();
    //定义变量记录聊天昵称
    private final String nickname;
    private Session session;

    public ChatEntpoint() {
        this.nickname = GUEST_PREFIX+connectionIds.getAndIncrement();
    }

    //客户端连接时自动进入
    @OnOpen
    public void start(Session session) {
        this.session=session;
        clientSet.add(this);
        String message = String.format("【%s %s】", nickname, "加入聊天室");
        broadcast(message);
    }

    @OnClose
    public void end() {
        clientSet.remove(this);
        String msg=String.format("【%s %s】", nickname, "离开了聊天室");
        broadcast(msg);
    }

    @OnMessage
    public void incoming(String message) {
        broadcast(message);
    }

    private void broadcast(String message) {
        for (ChatEntpoint client : clientSet) {
            try {
                synchronized (client) {
                    //发送消息
                    client.session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                System.out.println("聊天错误" + client);
                clientSet.remove(client);
                try{
                    client.session.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String msg = String.format("【%s %s】", client.nickname, "已经断开连接");
                broadcast(msg);
            }
        }
    }
}
