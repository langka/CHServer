package sse.xs.singleton

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by xusong on 2018/1/4.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class SDemo {
  val jInstance:JInstance = JInstance.GET()
  val greeting = "hello"

  def isNull:Boolean = jInstance==null
}
object SDemo extends App{
  val future = Future{
    300
  }
  future.foreach(a=>println(Thread.currentThread().toString))
  println(Thread.currentThread())
}

