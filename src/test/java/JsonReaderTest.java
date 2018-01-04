import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by xusong on 2018/1/4.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsonReaderTest {
    @Test
    public void test(){
        File file = new File("json");
        try {
            FileReader reader = new FileReader(file);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(jsonReader);
            JsonElement element1 = parser.parse(jsonReader);
            JsonElement element2 = parser.parse(jsonReader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ok");
    }
}
