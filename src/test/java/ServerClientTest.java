import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.junit.Test;
import sse.xs.conn.JsonConnection;
import sse.xs.msg.Message;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class ServerClientTest {

    @Test
    public void doTest(){
        new Thread(()->{
            try {
                Thread.sleep(1000);
                Socket s = new Socket("localhost",56432);
                JsonConnection connection = JsonConnection.createConnection(s);
                connection.writeJson(Message.createConnMessage().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            ServerSocket serverSocket = new ServerSocket(56432);

            Socket socket = serverSocket.accept();
            JsonConnection connection2 = JsonConnection.createConnection(socket);
            System.out.println(connection2.readJson());
            System.out.println("finish server client test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fileTest(){
        try {
            FileReader reader = new FileReader("json");
            JsonParser parser = new JsonParser();
            JsonReader reader1 = new JsonReader(reader);
            reader1.setLenient(true);
            JsonElement e=parser.parse(reader1);
            e = parser.parse(reader);
            e = parser.parse(reader);
            System.out.println("finish fileTest");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
