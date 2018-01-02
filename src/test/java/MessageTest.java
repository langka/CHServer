import com.google.gson.Gson;
import org.junit.Test;
import sse.xs.msg.data.CloseMsg;
import sse.xs.msg.Message;

/**
 * Created by xusong on 2017/11/27.dd
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class MessageTest {
    @Test
    public void writeParaMessage() {
        Message message = Message.createCloseConnMsg("服务器连接过多");
        Gson gson = new Gson();
        String x = gson.toJson(message);
        Message<CloseMsg> m = gson.fromJson(x, Message.class);
        String y = gson.toJson(m.data);
        CloseMsg closeMsg = gson.fromJson(y, CloseMsg.class);
        closeMsg.print();
    }
}
