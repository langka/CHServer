package sse.xs.msg.data.response;

import scala.util.parsing.combinator.testing.Str;

/**
 * Created by xusong on 2018/1/5.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 * join-leave-swap-create
 */
public class RoomResponse {
    public String roomName;
    public String roomKey;
    public boolean success;
    public String red;
    public String black;
    public int type;
    public String info;
    public String master;

    public AccountResponse r;
    public AccountResponse b;
}
