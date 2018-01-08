package sse.xs.server

import sse.xs.entity.OnlineUser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

/**
  * Created by xusong on 2018/1/3.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Sender(server: Server) {

  implicit def convertFunction(f: () => Unit): Runnable = {
    new Runnable {
      override def run(): Unit = f.apply()
    }
  }

  def sendMessage(msg: String, targetKey: String): Unit = {
    val f: OnlineUser => Unit = a => if (a != null) a.sendMessage(msg)
    if (targetKey != null)
      Future(server.getOnlineUser(targetKey)).foreach(f)
  }

}
object Sender extends App{
  val y = 1 until 1
  println(y)
}
