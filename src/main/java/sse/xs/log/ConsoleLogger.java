package sse.xs.log;

import sun.rmi.runtime.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class ConsoleLogger implements Logger{
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    public void log(String msg) {
        System.out.println(df.format(new Date()+msg));
    }
}
