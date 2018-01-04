package sse.xs.msg;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import sse.xs.entity.UserInfo;
import sse.xs.msg.data.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class Message<T> {
    public static final int TYPE_DISCONN = 1;
    public static final int TYPE_CONN = -1;

    public static final int TYPE_REGISTER = -2;
    public static final int TYPE_LOG_IN = 2;
    public static final int TYPE_LOG_OUT = 3;

    public static final int TYPE_TALK = 4;
    public static final int TYPE_MOVE = 5;

    public static final int TYPE_GET_ROOM = 6;
    public static final int TYPE_JOIN_ROOM = 7;
    public static final int TYPE_LEAVE_ROOM = 8;

    public static final int TYPE_STATICS = 9;


    public static final int TYPE_LOGIN_RESPONSE = 100;
    public static final int TYPE_LOGOUT_RESPONSE = 101;
    public static final int TYPE_REGISTER_RESPONSE = 102;
    public static final int TYPE_CONN_RESPOSE = 103;


    private static HashMap<Integer, Class> classMaps = new HashMap<>();

    static {
        classMaps.put(-1, ConnMsg.class);
        classMaps.put(2, LogInMsg.class);
        classMaps.put(-2, RegisterMsg.class);
        classMaps.put(3, LogOutMsg.class);


        classMaps.put(100, Response.class);
        classMaps.put(101, Response.class);
        classMaps.put(102, Response.class);
        classMaps.put(103,Response.class);
    }

    public int type;
    public String key;//每次操作的标识码,同一个标识码标志一个终端
    public T data;


    public static Gson gson = new Gson();

    public static Message createCloseConnMsg(String reason) {
        Message message = new Message<CloseMsg>();
        CloseMsg closeMsg = new CloseMsg();
        closeMsg.reason = reason;
        closeMsg.time = System.currentTimeMillis();
        closeMsg.retry = false;
        message.type = Message.TYPE_DISCONN;
        message.data = closeMsg;
        return message;
    }

    public static Message createConnResp(String key){
        Message<Response> message = new Message<>();
        message.type = TYPE_CONN_RESPOSE;
        message.key = key;
        message.data = new Response();
        message.data.success = true;
        message.data.info = "ok";
        return message;
    }

    public static Message createConnMessage() {
        Message message = new Message();
        ConnMsg connMsg = new ConnMsg();
        connMsg.state = 1;
        message.type = TYPE_CONN;
        message.data = connMsg;
        return message;
    }

    public static Message createLoginMessage(UserInfo userInfo) {
        Message message = new Message();
        LogInMsg msg = new LogInMsg();
        msg.info = userInfo;
        message.type = TYPE_LOG_IN;
        message.data = msg;
        return message;

    }

    public static ParameterizedType generateType(final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return Message.class;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * @return 真实的message对象
     */
    public static Optional<Message> getActualMessage(JsonElement element) {
        Message received = gson.fromJson(element, Message.class);
        Class clazz = classMaps.get(received.type);
        if(clazz==null)
            return Optional.empty();
        return Optional.of(gson.fromJson(element,generateType(clazz)));
    }

    @Override
    public String toString() {
        Class clazz = classMaps.get(type);
        ParameterizedType parameterizedType = generateType(clazz);
        return gson.toJson(this,parameterizedType);
    }
}
