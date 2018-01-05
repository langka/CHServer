package sse.xs.logic

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

import sse.xs.msg.Message
import sse.xs.msg.data.RoomRequest
import sse.xs.msg.data.response.RoomResponse
import sse.xs.server.Server

import scala.annotation.tailrec

/**
  * Created by xusong on 2018/1/5.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class RoomManager(server: Server) {
  val rooms: ConcurrentHashMap[String, Room] = ConcurrentHashMap[String, Room]
  val enterF: (Room, String) => Int = _.enter(_)
  val leaveF: (Room, String) => Int = _.leave(_)
  val swapF: (Room, String) => Int = (r, s) => r.swap()

  def handleRoomMessage(message: Message[RoomRequest]): Unit = {
    message.`type` match {
      case Message.TYPE_CREATE_ROOM => dealCreate(message)
      case Message.TYPE_JOIN_ROOM => dealEnter(message)
      case Message.TYPE_LEAVE_ROOM => dealLeave(message)
      case Message.TYPE_SWAP_ROOM => dealSwap(message)
    }
  }

  private def dealCreate(message: Message[RoomRequest]): Unit = {
    val roomRequest = message.data
    val name = if (roomRequest.name == "" || roomRequest.name == null) "快来一起战斗吧!" else roomRequest.name
    val pwd = if (roomRequest.password == "" || roomRequest.password == null) null else roomRequest.password
    val room = new Room(name = name, degree = roomRequest.degree, pwd = pwd, master = message.key)
    val roomKey = createRoom(room)
    room.roomKey = roomKey
    val msg = getResponse(room, Message.TYPE_CREATE_ROOM)
    sendToRoom(roomKey, msg.toString)
  }

  private def dealSwap(message: Message[RoomRequest]): Unit = {
    val result = swap(message.data.targetRoom, null)
    val msg = getResponse(getRoom(message.data.targetRoom), Message.TYPE_SWAP_ROOM)
    sendToRoom(message.data.targetRoom, msg.toString)
  }


  private def dealEnter(message: Message[RoomRequest]): Unit = {
    val result = enterRoom(message.data.targetRoom, message.key)
    if (result != 0) {
      val room = rooms.get(message.data.targetRoom)
      val msg = getResponse(room, Message.TYPE_JOIN_ROOM)
      sendToRoom(message.data.targetRoom, message.key)
    }
  }


  def dealLeave(message: Message[RoomRequest]): Unit = {
    val room = rooms.get(message.data.targetRoom)
    val msg = getResponse(room, Message.TYPE_LEAVE_ROOM)
    if (room.master == message.key) {
      //destroy the room
      rooms.remove(room.roomKey)
      sendToRoom(room, msg.toString)
    } else {
      //leave the room
      val red = room.red
      val black = room.black
      val str = msg.toString
      val result = leaveRoom(room.roomKey, message.key)
      if (result != 0) {
        server.getSender.sendMessage(str, red)
        server.getSender.sendMessage(str, black)
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
        sender.sendMessage(msg, room.red)
      if (room.black != null)
        sender.sendMessage(msg, room.black)
    }
  }


  private def sendToRoom(room: Room, msg: String): Unit = {
    if (room != null) {
      val sender = server.getSender
      if (room.red != null)
        sender.sendMessage(msg, room.red)
      if (room.black != null)
        sender.sendMessage(msg, room.black)
    }
  }

  private def getResponse(room: Room, tp: Int): Message[RoomResponse] = {
    val response = new RoomResponse
    val msg = new Message[RoomResponse]
    msg.`type` = tp
    response.`type` = tp
    response.roomKey = room.roomKey
    response.red = room.red
    response.black = room.black
    response.success = true
    response.roomName = room.name
    msg.data = response
    msg
  }

}

class Room(var name: String, var degree: Int = 0, pwd: String = null, val master: String) {
  var black: String = _
  var red: String = master
  val lock = new ReentrantLock
  var roomKey: String = _

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
