package sse.xs.server;

import sse.xs.conn.JsonConnection;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public interface ConnHandler {
     void applyFirst(JsonConnection connection);
     void applyRetry(JsonConnection connection,String key);


}
