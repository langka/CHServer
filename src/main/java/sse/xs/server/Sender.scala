package sse.xs.server

import java.io.IOException
import java.util.concurrent.{Executor, ExecutorService, Executors}

import scala.language.implicitConversions


/**
  * Created by xusong on 2018/1/3.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Sender {
  val senderThreads: ExecutorService = Executors.newFixedThreadPool(5)
  val server = Server.GET()

  implicit def convertFunction(f: () => Unit): Runnable = {
    new Runnable {
      override def run(): Unit = f.apply()
    }
  }

  def sendMessage(msg: String, targetKey: String): Unit = {
    val task: Runnable = () => {
      try {
        server.getOnlineUser(targetKey).sendMessage(msg)
      } catch {
        case e: IOException =>
        // TODO: 暂时什么都不做
      }
    }
    senderThreads.submit(task)

  }

}
