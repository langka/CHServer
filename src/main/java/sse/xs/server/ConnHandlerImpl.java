package sse.xs.server;

import sse.xs.conn.JsonConnection;
import sse.xs.entity.OnlineUser;
import sse.xs.msg.Message;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class ConnHandlerImpl implements ConnHandler {

    ConcurrentHashMap<String, OnlineUser> players = new ConcurrentHashMap<>();
    //用于生成随机数字作为签名
    Random random = new Random();


    @Override
    public void applyFirst(JsonConnection connection) {
        OnlineUser onlineUser = new OnlineUser();
        onlineUser.setConnection(connection);
        for (String key; ;) {//循环直至放置成功
            key= UUID.randomUUID().toString();
            OnlineUser previous = players.putIfAbsent(key, onlineUser);
            if (previous == null) {//put成功
                onlineUser.prepare();
                break;
            }
        }
    }

    @Override
    public void applyRetry(JsonConnection connection, String key) {

    }

    @Override
    public OnlineUser getByKey(String key) {
        return players.get(key);
    }
}
