package sse.xs.entity;

import com.google.gson.JsonElement;
import sse.xs.conn.JsonConnection;
import sse.xs.msg.Message;
import sse.xs.server.MessageListener;
import sse.xs.msg.Message.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class OnlineUser {

    private static ExecutorService listenThreads = Executors.newCachedThreadPool();

    private final UserInfo userInfo;
    private JsonConnection connection;
    //消息监听器
    private MessageListener listener ;

    // TODO: 2018/1/3 完成全部的message类型
    public OnlineUser() {
        userInfo = new UserInfo();
        setListener((message)->{
            switch (message.type){
                case Message.
            }
        });
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void setConnection(JsonConnection connection) {
        this.connection = connection;
    }

    public void resetConnection(JsonConnection connection) {
        this.connection.close();
        this.connection = connection;
    }

    //连接建立好后准备阶段,此时已经没有connmsg
    public void prepare() {
        listenThreads.submit(() -> {
            for (; ; ) {
                Optional<JsonElement> optional = connection.readJsonNoException();
                if (optional.isPresent()) {
                    Optional<Message> current = Message.getActualMessage(optional.get());
                    if(current.isPresent()){
                        listener.onNewMessage(current.get());
                    }else{//错误的消息类型
                        // TODO: 2018/1/3 打印到错误文件
                    }
                } else {
                    break;
                }
            }
            connection.close();
        });
    }
}
