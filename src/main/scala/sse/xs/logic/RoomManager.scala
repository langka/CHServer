package sse.xs.logic

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

import sse.xs.msg.Message
import sse.xs.msg.data.{GameRequest, MoveRequest, RoomRequest}
import sse.xs.msg.data.response.RoomResponse
import sse.xs.server.{Sender, Server}

import scala.annotation.tailrec

/**
  * Created by xusong on 2018/1/5.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class RoomManager(server: Server) {
  val rooms: ConcurrentHashMap[String, Room] = new ConcurrentHashMap[String, Room]
  val enterF: (Room, String) => Int = _.enter(_)
  val leaveF: (Room, String) => Int = (r, s) => if (r == null) 0 else r.leave(s)
  val swapF: (Room, String) => Int = (r, s) => r.swap()

  def handleRoomMessage(message: Message[_]): Unit = {
    message.`type` match {
      case Message.TYPE_CREATE_ROOM => dealCreate(message.asInstanceOf[Message[RoomRequest]])
      case Message.TYPE_JOIN_ROOM => dealEnter(message.asInstanceOf[Message[RoomRequest]])
      case Message.TYPE_LEAVE_ROOM => dealLeave(message.asInstanceOf[Message[RoomRequest]])
      case Message.TYPE_SWAP_ROOM => dealSwap(message.asInstanceOf[Message[RoomRequest]])
      case Message.TYPE_GAME_REQUEST =>rooms.get(message.asInstanceOf[Message[GameRequest]].data.room).game.handleGameMessage(message)
      case Message.TYPE_MOVE_REQUEST =>rooms.get(message.asInstanceOf[Message[MoveRequest]].data.room).game.handleGameMessage(message)
    }
  }

  def getRooms:java.util.Collection[Room] = rooms.values()


  private def dealCreate(message: Message[RoomRequest]): Unit = {
    val roomRequest = message.data
    val name = if (roomRequest.name == "" || roomRequest.name == null) "快来一起战斗吧!" else roomRequest.name
    val pwd = if (roomRequest.password == "" || roomRequest.password == null) null else roomRequest.password
    val room = new Room(name = name, degree = roomRequest.degree, pwd = pwd, master = message.key,server.getSender)
    val roomKey = createRoom(room)
    room.roomKey = roomKey
    val msg = getRoomResponse(message.id,room, Message.TYPE_ROOM_RESPONSE, Message.TYPE_CREATE_ROOM)
    sendToRoom(roomKey, msg.toString)
  }

  private def dealSwap(message: Message[RoomRequest]): Unit = {
    val result = swap(message.data.targetRoom, null)
    val msg = getRoomResponse(message.id,getRoom(message.data.targetRoom), Message.TYPE_ROOM_RESPONSE, Message.TYPE_SWAP_ROOM)
    sendToRoom(message.data.targetRoom, msg.toString)
  }

  private def dealEnter(message: Message[RoomRequest]): Unit = {
    val result = enterRoom(message.data.targetRoom, message.key)
    val room = rooms.get(message.data.targetRoom)
    if (result != 0) {
      val msg = getRoomResponse(message.id,room, Message.TYPE_ROOM_RESPONSE, Message.TYPE_JOIN_ROOM)
      sendToRoom(message.data.targetRoom, message.toString)
    } else {
      //房间加入失败
      val msg = getRoomResponse(message.id,room, Message.TYPE_ROOM_RESPONSE, Message.TYPE_JOIN_ROOM, success = false)
      server.getSender.sendMessageAsync(msg.toString, message.key)
    }
  }


  def dealLeave(message: Message[RoomRequest]): Unit = {
    val room = rooms.get(message.data.targetRoom)
    val msg = getRoomResponse(message.id,room, Message.TYPE_ROOM_RESPONSE, Message.TYPE_LEAVE_ROOM)
    if (room.master == message.key) {
      //destroy the room
      rooms.remove(room.roomKey)
      msg.data.info = "房主取消了房间"
      msg.data.red = null
      msg.data.black = null
      sendToRoom(room, msg.toString)
    } else {
      //leave the room
      val red = room.red
      val black = room.black
      val str = msg.toString
      val result = leaveRoom(room.roomKey, message.key)
      if (result != 0) {
        if(result==1)
          msg.data.red = null
        else if(result==2)
          msg.data.black = null
        server.getSender.sendMessageAsync(str, red)
        server.getSender.sendMessageAsync(str, black)
      } else {
        //离开这个房间fail：reason：房间失效  reason:不在这个房间
        val fail = getRoomResponse(message.id,room, Message.TYPE_ROOM_RESPONSE, Message.TYPE_LEAVE_ROOM, success = false)
        server.getSender.sendMessageAsync(false.toString, message.key)
      }
    }
  }


  @tailrec
  private final def createRoom(room: Room): String = {
    val key = UUID.randomUUID().toString
    val result = rooms.putIfAbsent(key, room)
    if (result == null)
      key
    else createRoom(room)
  }

  private def roomOp(f: (Room, String) => Int)(roomKey: String, key: String): Int = f(rooms.get(roomKey), key)

  private def enterRoom: (String, String) => Int = roomOp(enterF)

  private def leaveRoom: (String, String) => Int = roomOp(leaveF)

  private def swap: (String, String) => Int = roomOp(swapF)

  def getRoom(key: String): Room = rooms.get(key)

  private def sendToRoom(roomKey: String, msg: String): Unit = {
    val room = rooms.get(roomKey)
    if (room != null) {
      val sender = server.getSender
      if (room.red != null)
        sender.sendMessageAsync(msg, room.red)
      if (room.black != null)
        sender.sendMessageAsync(msg, room.black)
    }
  }


  private def sendToRoom(room: Room, msg: String): Unit = {
    if (room != null) {
      val sender = server.getSender
      if (room.red != null)
        sender.sendMessageAsync(msg, room.red)
      if (room.black != null)
        sender.sendMessageAsync(msg, room.black)
    }
  }

  /**
    *
    * @param room
    * @param tp       指示序列化的类，稳定为 RoomResponse.class
    * @param actualTp 指示具体的操作
    * @return
    */

  private def getRoomResponse(id:Int,room: Room, tp: Int, actualTp: Int, success: Boolean = true): Message[RoomResponse] = {
    val response = new RoomResponse
    val msg = new Message[RoomResponse]
    msg.`type` = tp
    msg.id = id
    response.`type` = actualTp
    response.roomKey = room.roomKey
    response.master = room.master
    response.red = room.red
    response.black = room.black
    response.success = success
    response.roomName = room.name
    msg.data = response
    msg
  }

}

class Room(var name: String, var degree: Int = 0, pwd: String = null, val master: String,val sender:Sender) {
  @volatile var black: String = _
  @volatile var red: String = master
  val lock = new ReentrantLock
  var roomKey: String = _
  val game = new Game(this)

  def swap(): Int = {
    val temp = black
    black = red
    red = temp
    1
  }

  //红色:1 黑色2 0失败
  def enter(key: String): Int = {
    try {
      lock.lock()
      if (red != null && black != null)
        0
      else {
        if (red == null) {
          red = key
          1
        } else {
          black = key
          2
        }
      }
    } finally
      lock.unlock()
  }

  def kick(): Int = {
    1
  }

  def leave(key: String): Int = {
    if (red == key) {
      red = null
      1
    }
    else if (black == key) {
      black = null
      2
    }
    0
  }

  def vacant: Int = {
    if (red == null)
      1
    else if (black == null)
      2
    else
      0
  }

  def rename(name: String): Unit = {
    this.name = name
  }

}

