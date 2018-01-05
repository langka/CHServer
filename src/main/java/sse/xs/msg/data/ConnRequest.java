package sse.xs.msg.data;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class ConnRequest {
    public static int STATE_FIRST = 1;
    public static int STATE_RETRY = 2;

    public String key;
    public int state;//1 第一次，2 retry

}
