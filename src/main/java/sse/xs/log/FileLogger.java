package sse.xs.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xusong on 2018/1/2.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class FileLogger implements Logger {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    File file;
    FileWriter writer;

    public FileLogger(String name) {
        file = new File(name);
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String msg) {
        try {
            writer.write(df.format(new Date()) + " " + msg);
        } catch (IOException e) {
            System.out.println(df.format(new Date()) + "cannot write msg: " +msg );
            e.printStackTrace();
        }

    }
}
