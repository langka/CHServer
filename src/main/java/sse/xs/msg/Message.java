package sse.xs.msg;

import sse.xs.msg.data.CloseMsg;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class Message<T> {
    public static int TYPE_CLOSE = 1;
    public static int TYPE_CONN = -1;

    public int type;
    public T data;

    @Override
    public String toString() {
        return "type: "+type+" data: "+data;
    }

    public static Message createCloseConnMsg(String reason){
        Message message = new Message<CloseMsg>();
        CloseMsg closeMsg = new CloseMsg();
        closeMsg.reason = reason;
        closeMsg.time = System.currentTimeMillis();
        closeMsg.retry = false;
        message.data = closeMsg;
        return message;
    }

    public static ParameterizedType generateType(final Type... args){
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

}
