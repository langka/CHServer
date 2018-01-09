package sse.xs.msg.data;

/**
 * Created by xusong on 2018/1/9.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 * 1 red jj
 * 2 black jj
 * 3 red win
 * 4 black win
 * 5 nothing,just change the turn its red turn
 * 6 its black turn
 */
public class TurnChangeMsg {
    public int state;
    public TurnChangeMsg(int state){
        this.state = state;
    }
}
