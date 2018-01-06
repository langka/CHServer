package sse.xs.logic

import sse.xs.msg.Message

/**
  * Created by xusong on 2018/1/6.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Game(room: Room) {

  def start():Message[_] = {

  }

  def nextStep(step: Step): Unit = {

  }

}

case class Step(from: Pos, to: Pos)

case class Pos(x: Int, y: Int)