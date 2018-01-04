package sse.xs.singleton;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JInstance {
    private static JInstance instance = new JInstance();
    public static JInstance GET(){
        return instance;
    }

    SDemo sDemo = new SDemo();
    JDemo jDemo = new JDemo();

    public static void main(String[] args) {
        JInstance instance = JInstance.GET();
    }
}
