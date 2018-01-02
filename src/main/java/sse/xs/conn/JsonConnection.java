package sse.xs.conn;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import sse.xs.msg.Message;

import java.io.*;
import java.net.Socket;

/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsonConnection {
    private Socket socket;
    private Gson gson = new Gson();

    private Writer writer;
    private InputStreamReader reader;

    private JsonParser parser;

    public static JsonConnection createConnection(Socket socket){
        try {
            return new JsonConnection(socket);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonConnection(Socket socket) throws IOException {
        this.socket = socket;
        writer = new OutputStreamWriter(socket.getOutputStream());
        reader = new InputStreamReader(socket.getInputStream());
    }

    public JsonElement readJson() throws IOException {
        return parser.parse(reader);
    }

    // TODO: 2018/1/2 写入正确的数据
    public void writeJson(Message msg) throws IOException {
        writer.write(msg.toString());
    }

    public void close()  {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
