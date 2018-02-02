package sse.xs.msg.data.response;

import sse.xs.logic.Room;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xusong on 2018/1/12.
 * About:
 */

public class StaticsResponse {
    public List<RoomData> rooms;

    public static StaticsResponse createFromRooms(Collection<Room> roomlist){
        StaticsResponse response = new StaticsResponse();
        List<RoomData> roomDatas = new LinkedList<>();
        if(roomlist==null||roomlist.size()==0)
            return response;
        else{
            for(Room current:roomlist){
                RoomData data = new RoomData(current.name(),current.roomKey());
                roomDatas.add(data);
            }
            response.rooms = roomDatas;
            return response;
        }

    }

    public static StaticsResponse demoResponse(){
        StaticsResponse response = new StaticsResponse();
        List<RoomData> roomDatas = new LinkedList<>();
        roomDatas.add(new RoomData("hello","good"));
        roomDatas.add(new RoomData("886","ok"));
        response.rooms = roomDatas;
        return response;
    }
}
class RoomData{
    public String name;//房间名与房间id
    public String key;
    public RoomData(){

    }

    public RoomData(String name, String key) {
        this.name = name;
        this.key = key;
    }
}