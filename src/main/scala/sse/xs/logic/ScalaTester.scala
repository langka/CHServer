package sse.xs.logic

import java.io.PrintWriter
import java.net.Socket

import sse.xs.conn.JsonConnection
import sse.xs.entity.UserInfo
import sse.xs.msg.Message
import sse.xs.msg.data.response.{AccountResponse, RoomResponse}

/**
  * Created by xusong on 2018/1/5.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class ScalaTester {
  def createMessageSequence(): Unit = {
    val conn = Message.createConnMessage()
    val connection = JsonConnection.createConnection(new Socket("localhost", 9876))
    connection.writeJson(conn.toString)
    val r1 = connection.readMessage().asInstanceOf[Message[AccountResponse]]

    val login = Message.createLoginMessage(new UserInfo("xusong", "xusong"))
    login.key = r1.key
    connection.writeJson(login.toString)
    val r2 = connection.readMessage.asInstanceOf[Message[AccountResponse]]

    val create = Message.createCreateRoomRequest(r1.key, "happy!", 0, "happy")
    connection.writeJson(create.toString)
    val r3 = connection.readMessage.asInstanceOf[Message[RoomResponse]]


    val conn2 = Message.createConnMessage()
    val connection2 = JsonConnection.createConnection(new Socket("localhost",9876))
    connection2.writeJson(conn2.toString)
    val k1 = connection2.readMessage().asInstanceOf[Message[AccountResponse]]


    val login2 = Message.createLoginMessage(new UserInfo("xusong", "xusong"))
    login2.key = k1.key
    connection.writeJson(login2.toString)
    val k2 = connection2.readMessage().asInstanceOf[Message[AccountResponse]]

    val join = Message.createJoinRoomRequest(r3.data.roomKey,k1.key)
    connection2.writeJson(join.toString)
    val k3 = connection2.readMessage().asInstanceOf[Message[RoomResponse]]
    val r5 = connection.readMessage().asInstanceOf[Message[RoomResponse]]

    val roomKey = r3.data.roomKey
    val leave = Message.createLeaveRoomRequest(roomKey, r1.key)
    connection.writeJson(leave.toString)
    val r4 = connection.readMessage
    val k4 = connection2.readMessage


    val writer = new PrintWriter("first.jsons")
    val writer2 = new PrintWriter("second.jsons")
    val responses = r1 +: r2 +: r3 +: r5+:r4 +:Nil
    val responses2 = k1+:k2+:k3+:k4+:Nil
    responses.foreach(x => writer.write(x.toString))
    responses2.foreach(x=>writer2.write(x.toString))
    writer.close()
    writer2.close()
  }


}
