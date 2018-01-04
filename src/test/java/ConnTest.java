import com.google.gson.Gson;
import org.junit.Test;
import sse.xs.conn.JsonConnection;
import sse.xs.entity.UserInfo;
import sse.xs.msg.Message;
import sse.xs.msg.data.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class ConnTest {

    static Gson gson = new Gson();
    public void connWithResponse(String filename){
        File file = new File(filename);
        try {
            Socket socket = new Socket("localhost",9876);
            JsonConnection connection = JsonConnection.createConnection(socket);
            Message m = Message.createConnMessage();
            Message login = Message.createLoginMessage(new UserInfo("xusong","xusong"));
            Message log2 = Message.createLoginMessage(new UserInfo("gaoyuan","gaoyuan"));
            connection.writeJson(m.toString());
            Message<Response> coResp=Message.getActualMessage(connection.readJson()).get();
            String key = coResp.key;
            login.key = key;
            log2.key = key;
            connection.writeJson(login.toString());
            connection.writeJson(log2.toString());
            String s2 = Message.getActualMessage(connection.readJson()).get().toString();
            String s3 = Message.getActualMessage(connection.readJson()).get().toString();
            FileWriter writer = new FileWriter(file);
            writer.write(coResp.toString());
            writer.write(s2);
            writer.write(s3);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void connTest(){
        connWithResponse("connTest.jsons");
    }
}
