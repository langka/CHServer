package sse.xs.logic;

import sse.xs.entity.OnlineUser;
import sse.xs.msg.Message;
import sse.xs.msg.data.LogInMsg;
import sse.xs.msg.data.Response;
import sse.xs.server.Server;

/**
 * Created by xusong on 2018/1/3.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class UserController {
    private static final UserController instance = new UserController();
    private Server server = Server.GET();

    private UserController() {

    }

    public static UserController getInstance() {
        return instance;
    }

    public void handleLogIn(Message message) {
        Message<LogInMsg> logInMsgMessage = message;
        Message<Response> responseMessage = new Message<>();
        responseMessage.type = Message.TYPE_LOGIN_RESPONSE;
        if (logInMsgMessage.data.info.name.equals("xusong") && logInMsgMessage.data.info.pwd.equals("xusong")) {//能够成功登陆
            OnlineUser user = server.getOnlineUser(message.key);
            user.setUserInfo(logInMsgMessage.data.info);
            //发送登陆成功的消息
            Response response = new Response();
            response.success = true;
            response.info = "登陆成功！";
            responseMessage.data = response;
        } else {//发送登陆失败的消息
            Response response = new Response();
            response.success = false;
            response.info = "账号或密码错误";
            responseMessage.data = response;
        }
        server.getSender().sendMessage(responseMessage.toString(), message.key);
    }

    public void handleLogOut(Message message) {

    }

    public void handleRegister(Message message) {

    }

}
