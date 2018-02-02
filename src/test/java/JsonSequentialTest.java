import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import scala.util.parsing.combinator.testing.Str;
import sse.xs.entity.UserInfo;
import sse.xs.msg.Message;
import sse.xs.msg.data.response.StaticsResponse;

import java.io.*;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsonSequentialTest {
    public void writeToFile(){
        File file = new File("json");
        Message conn = Message.createConnMessage();
        UserInfo info1 =new UserInfo("xusong","xusong");
        UserInfo info2 = new UserInfo("gaoyuan","gaoyuan");
        Message login = Message.createLoginMessage(info1);
        Message login2 = Message.createLoginMessage(info2);
        try {
            FileWriter writer = new FileWriter(file);
            String a = conn.toString();
            String b = login.toString();
            String c = login2.toString();
            writer.write(a);
            writer.write(b);
            writer.write(c);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void writeComplicatedMsg(){
        StaticsResponse response =StaticsResponse.demoResponse();
        Message<StaticsResponse> message = new Message<>();
        message.type=Message.TYPE_STATICS_RESP;
        message.data = response;
        FileWriter writer = null;
        try {
             writer = new FileWriter("statics.json");
            writer.write(message.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(writer!=null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Test
    public void readBack(){
        try {
            FileReader reader = new FileReader("statics.json");
            JsonReader jsonReader = new JsonReader(reader);
            JsonParser parser = new JsonParser();
            JsonElement element=parser.parse(jsonReader);
            Message m=Message.getActualMessage(element).get();
            System.out.println(m);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
