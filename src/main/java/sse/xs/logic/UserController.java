package sse.xs.logic;

import sse.xs.entity.OnlineUser;
import sse.xs.msg.Message;
import sse.xs.msg.data.LogInRequest;
import sse.xs.msg.data.response.AccountResponse;
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
        Message<AccountResponse> responseMessage = new Message<>();
        responseMessage.type = Message.TYPE_LOGIN_RESPONSE;
        if (checkUser(((Message<LogInRequest>) message).data.info.name,((Message<LogInRequest>) message).data.info.pwd)) {//能够成功登陆
            OnlineUser user = server.getOnlineUser(message.key);
            user.setUserInfo(((Message<LogInRequest>) message).data.info);
            //发送登陆成功的消息
            responseMessage.id = message.id;
            AccountResponse response = new AccountResponse();
            response.success = true;
            response.info = "登陆成功！";
            responseMessage.data = response;
        } else {//发送登陆失败的消息
            responseMessage.id = message.id;
            AccountResponse response = new AccountResponse();
            response.success = false;
            response.info = "账号或密码错误";
            responseMessage.data = response;
        }
        server.getSender().sendMessageAsync(responseMessage.toString(), message.key);
    }

    public boolean checkUser(String name,String pwd){
        if(name.equals("xusong")&&pwd.equals("xusong"))
            return true;
        else return name.equals("gaoyuan") && pwd.equals("gaoyuan");
    }

    public void handleLogOut(Message message) {

    }

    public void handleRegister(Message message) {

    }



}
