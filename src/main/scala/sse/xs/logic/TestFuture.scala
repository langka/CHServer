package sse.xs.logic

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xusong on 2018/1/9.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object TestFuture extends App {
  val a = Array(1,2,3)
  val b = Array(5,6,7)
  val c = Array(2,5,3)
  val list = Array(a,b,c).toList
  println(list)
  println(list.flatten)

}
