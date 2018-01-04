import org.junit.Test;
import scala.util.parsing.combinator.testing.Str;
import sse.xs.entity.UserInfo;
import sse.xs.msg.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsonSequentialTest {
    @Test
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
}
