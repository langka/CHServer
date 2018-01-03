package sse.xs.entity;

import com.google.gson.JsonElement;
import sse.xs.conn.JsonConnection;
import sse.xs.logic.UserController;
import sse.xs.msg.Message;
import sse.xs.server.MessageListener;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class OnlineUser {

    //监听每一个入口
    private static ExecutorService listenThreads = Executors.newCachedThreadPool();
    //根据消息类型执行任务
    private static ExecutorService dispatchThreads = Executors.newFixedThreadPool(5);

    private final UserInfo userInfo;
    private JsonConnection connection;


    //消息监听器
    private final MessageListener listener = (message) -> dispatchThreads.submit(getTask(message));

    private static Runnable getTask(Message m) {
        switch (m.type) {
            case Message.TYPE_LOG_IN:
                return () -> UserController.getInstance().handleLogIn(m);
            case Message.TYPE_LOG_OUT:
                return () -> UserController.getInstance().handleLogOut(m);
            case Message.TYPE_REGISTER:
                return () -> UserController.getInstance().handleRegister(m);
        }
        return () -> {
        };
    }

    // TODO: 2018/1/3 完成全部的message类型
    public OnlineUser() {
        userInfo = new UserInfo();
    }

    public void setUserInfo(UserInfo info) {
        this.userInfo.name = info.name;
        this.userInfo.pwd = info.pwd;
    }

    public void setConnection(JsonConnection connection) {
        this.connection = connection;
    }

    public void resetConnection(JsonConnection connection) {
        this.connection.close();
        this.connection = connection;
    }

    public void sendMessage(String message) throws IOException {
        connection.writeJson(message);
    }

    /**
     * message dispatch
     */
    private void handleLogIn(Message message) {

    }

    private void handleLogOut(Message message) {

    }

    //连接建立好后准备阶段,此时已经没有connmsg
    public void prepare() {
        listenThreads.submit(() -> {
            for (; ; ) {
                Optional<JsonElement> optional = connection.readJsonNoException();
                if (optional.isPresent()) {
                    Optional<Message> current = Message.getActualMessage(optional.get());
                    if (current.isPresent()) {
                        listener.onNewMessage(current.get());
                    } else {//错误的消息类型
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
