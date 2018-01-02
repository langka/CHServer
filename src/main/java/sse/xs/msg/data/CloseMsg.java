package sse.xs.msg.data;

/**
 * Created by xusong on 2017/11/27.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class CloseMsg {
   public String  reason;
    public long time;
    public boolean retry;
    public void print(){
        System.out.println(reason+retry);
    }
}
