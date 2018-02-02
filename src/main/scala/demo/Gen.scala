package demo

import java.io.{FileOutputStream, FileWriter}
import java.util
import java.util.UUID

import com.google.gson.Gson
import sse.xs.msg.Message
import sse.xs.msg.data.response.{AccountResponse, RoomData, StaticsResponse}

/**
  * Created by xusong on 2018/1/22.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Gen {

}
object Gen extends App{
  val gson = new Gson
  val writer  = new FileWriter("demo")
  val key = UUID.randomUUID().toString
  val m1 = new Message[AccountResponse]
  val r1 = new AccountResponse
  r1.success = true
  m1.data = r1
  m1.`type` = Message.TYPE_LOGIN_RESPONSE
  writer.write(gson.toJson(m1))

  val m2 = new Message[StaticsResponse]
  val r2 = StaticsResponse.demoResponse
  m2.`type` = Message.TYPE_STATICS_RESP
  m2.data = r2
  writer.write(gson.toJson(m2))

  writer.close()
}
