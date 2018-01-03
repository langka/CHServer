package sse.xs.server;

import sse.xs.msg.Message;

/**
 * Created by xusong on 2018/1/3.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public interface MessageListener {
     void onNewMessage(Message message);
}
