package sse.xs.msg;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.omg.CORBA.PUBLIC_MEMBER;
import sse.xs.msg.data.CloseMsg;
import sse.xs.msg.data.ConnMsg;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class Message<T> {
    public static final int TYPE_CLOSE = 1;
    public static final int TYPE_CONN = -1;

    public static final int TYPE_REGISTER =-2;
    public static final int TYPE_LON_IN = 2;
    public static final int TYPE_LOG_OUT = 3;

    public static final int TYPE_TALK = 4;
    public static final int TYPE_MOVE = 5;

    public static final int TYPE_GET_ROOM = 6;
    public static final int TYPE_JOIN_ROOM = 7;
    public static final int TYPE_LEAVE_ROOM = 8;

    public static final int TYPE_STATICS = 9;


    public int type;
    public T data;


    public static Gson gson = new Gson();

    @Override
    public String toString() {
        return "type: " + type + " data: " + data;
    }

    public static Message createCloseConnMsg(String reason) {
        Message message = new Message<CloseMsg>();
        CloseMsg closeMsg = new CloseMsg();
        closeMsg.reason = reason;
        closeMsg.time = System.currentTimeMillis();
        closeMsg.retry = false;
        message.data = closeMsg;
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
    // TODO: 2018/1/3 添加更多的类型识别
    public static Optional<Message> getActualMessage(JsonElement element) {
        Message received = gson.fromJson(element, Message.class);
        switch (received.type) {
            case TYPE_CLOSE:
                return Optional.of(gson.fromJson(element, generateType(CloseMsg.class)));
            case TYPE_CONN:
                return Optional.of(gson.fromJson(element, generateType(ConnMsg.class)));
        }
        return Optional.empty();
    }


}
