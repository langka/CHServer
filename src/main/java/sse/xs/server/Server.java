package sse.xs.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import sse.xs.conn.JsonConnection;
import sse.xs.msg.Message;
import sse.xs.msg.data.ConnMsg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static sse.xs.msg.data.ConnMsg.STATE_FIRST;
import static sse.xs.msg.data.ConnMsg.STATE_RETRY;

/**
 * Created by xusong on 2017/11/25.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class Server {
    public static void main(String[] args) {
        Server server = Server.create();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    static final int CONNCOUNT = 1000;

    private final AtomicInteger bound = new AtomicInteger(0);
    //这个线程池负责接受连接请求
    private ExecutorService connExec = Executors.newCachedThreadPool();

    private ConnHandler connHandler = new ConnHandlerImpl();



    /*
      static methods
     */

    public static Server create() {
        return new Server();
    }

    /*

     */

    public Server() {

    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(9876);
        int threads = 0;
        while (true) {
            Socket socket = serverSocket.accept();
            connExec.submit(new AcceptTask(connHandler,socket, bound));
        }
    }

}

class AcceptTask implements Runnable {
    static Gson gson = new Gson();
    JsonConnection jsonConnection;
    ConnHandler handler;
    AtomicInteger bound;
    public AcceptTask(ConnHandler handler,Socket socket, AtomicInteger bound) {
        jsonConnection = JsonConnection.createConnection(socket);
        this.bound = bound;
    }

    public void run() {
        if (jsonConnection == null)
            return;
        int i = bound.incrementAndGet();
        if (i > Server.CONNCOUNT) {//超限
            Message message = Message.createCloseConnMsg("服务器连接超过上限");
            try {
                jsonConnection.writeJson(message);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                jsonConnection.close();
            }
        } else {//允许连接，接受第一条连接请求
            try {
                JsonElement element = jsonConnection.readJson();
                Message received=gson.fromJson(element,Message.class);
                if(received.type!=Message.TYPE_CONN){
                    Message reason = Message.createCloseConnMsg("非法的连接请求，请重试");
                    jsonConnection.writeJson(reason);
                    jsonConnection.close();
                }
                else{//连接建立，转化为正确的泛型
                    Message<ConnMsg> connMessage = gson.fromJson(element,Message.generateType(ConnMsg.class));
                    if(connMessage.data.state==STATE_RETRY){
                        handler.applyRetry(jsonConnection,connMessage.data.key);
                    }else if(connMessage.data.state==STATE_FIRST){//
                        handler.applyFirst(jsonConnection);
                    }else{

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

}

