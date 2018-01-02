import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import sse.xs.msg.Message;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xusong on 2017/11/27.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsParseTest {
    @Test
    public void parseTest(){
        Gson gson = new Gson();
        Student student = new Student(3,"xusong",1);
        Student student1 = new Student(4,"wwww",2);
        try {
            FileWriter writer = new FileWriter("hello.txt");
            String a=gson.toJson(student);
            writer.write(a);
            String b = gson.toJson(student1);
            writer.write(b);
            writer.close();
            JsonParser parser = new JsonParser();
            JsonReader reader = new JsonReader(new FileReader("hello.txt"));
            Message message = gson.fromJson(reader,Message.class);
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Student {
    int age;
    String name;
    int grade;

    public Student(int age, String name, int grade) {
        this.age = age;
        this.name = name;
        this.grade = grade;
    }
}
