package sse.xs.logic

import sse.xs.msg.Message
import sse.xs.msg.data.{GameRequest, MoveRequest}
import sse.xs.msg.data.response.{GameResponse, MoveResponse}
import sse.xs.server.Sender

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xusong on 2018/1/6.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Game(room: Room) {

  @volatile var redTurn = true
  @volatile var state = 0
  //0未开始 1游戏中 2结束
  val sender: Sender = room.sender
  val checkerboard: Array[Array[Pie]] = new Array[Array[Pie]](9) map (x => new Array[Pie](10))

  def handleGameMessage(request: Message[_]): Unit = getMessage(request) onSuccess {
    case e => e._1 foreach (x => sender.sendMessage(e._2.toString, x))
  }

  def getMessage(request: Message[_]): Future[(List[String], Message[_])] = request.`type` match {
    case Message.TYPE_GAME_REQUEST =>
      val m = request.asInstanceOf[Message[GameRequest]]
      if (m.data.`type` == 1) startMessage(m) else endMessage(m)
    case Message.TYPE_MOVE_REQUEST => moveMessage(request.asInstanceOf[Message[MoveRequest]])
  }

  def startMessage(request: Message[GameRequest]): Future[(List[String], Message[GameResponse])] = {
    Future {
      if (room.red == null || room.black == null || state != 0)
        false
      else {
        resetPieces()
        redTurn = true
        true
      }
    } map {
      if (_) {
        state = 0
        (List(request.key), startFailedMessage())
      }
      else {
        state = 1
        (List(room.red, room.black), startSuccessMessage())
      }
    }
  }

  def endMessage(request: Message[GameRequest]): Future[(List[String], Message[GameResponse])] = {
    null
  }

  def moveMessage(request: Message[MoveRequest]): Future[(List[String], Message[MoveResponse])] = {
    Future {
      val isRed: Boolean = request.key == room.red
      if (isRed == redTurn) {
        //我的回合
        val from = Pos(request.data.fromX, request.data.fromY)
        val to = Pos(request.data.toX, request.data.toY)
        moveStep(Step(from, to, request.key))
      } else {//不是我的回合
        // TODO: 发送消息
        null
      }
    }
  }

  private def moveStep(step: Step): (List[String], Message[MoveResponse]) = {
    val isRed = step.mover == room.red
    if (checkerboard(step.from.y)(step.from.x) == Blank) {
      //空子
      val msg = failMoveMessage()
      (List(step.mover), msg)
    } else {
      val piece = checkerboard(step.from.x)(step.from.y).asInstanceOf[Piece]
      if (piece.red == isRed) {
        //颜色对了 走的是自己的棋子
        val f: (Int, Int) => Pie = (a, b) => checkerboard(a)(b)
        implicit def intToF3(status: Int): (Pos, Pos, Boolean) => Boolean = (x, y, b) => MoveLaws.laws(status)(x, y, b, f)
        // TODO: 完成正确的移动/胜负判定以及发送消息
        if (canMove(step.from, step.to, piece.red, piece.status)) {
          null
        } else {
          val msg = failMoveMessage()
          (List(step.mover), msg)
        }
      } else {
        //棋子颜色不对
        val msg = failMoveMessage()
        (List(step.mover), msg)
      }
    }
  }

  private def canMove(f: Pos, t: Pos, red: Boolean, law: (Pos, Pos, Boolean) => Boolean) = law(f, t, red)


  private def failMoveMessage(info:String = null): Message[MoveResponse] = {
    val message = new Message[MoveResponse]
    message.`type` = Message.TYPE_MOVE_RESPONSE
    val data = new MoveResponse
    data.success = false
    data.info = info
    message.data = data
    message
  }

  private def startFailedMessage(): Message[GameResponse] = {
    val message = new Message[GameResponse]
    val response = new GameResponse
    response.success = false
    response.info = "玩家数量不足！"
    message.data = response
    message.`type` = Message.TYPE_GAME_RESPONSE
    message
  }

  private def startSuccessMessage(): Message[GameResponse] = {
    val message = new Message[GameResponse]
    val response = new GameResponse
    response.success = true
    response.info = "游戏开始！"
    message.data = response
    message.`type` = Message.TYPE_GAME_RESPONSE
    message
  }

  private def cleanPieces(pieces: Array[Pie]): Unit = {
    for (i <- 0 to 9) pieces(i) = Blank
  }

   def resetPieces(): Unit = {
    checkerboard foreach cleanPieces
    resetSoldiers()
    resetCannon()
    resetVehicle()
    resetHorse()
    resetElephant()
    resetGuard()
    resetGeneral()
  }

  implicit def intToFunction(x: Int): Int => Boolean = _ == x

  private def resetSoldiers(): Unit = reset(Pieces.soldier, List(0, 2, 4, 6, 8), List(3, 6), 6)

  private def resetCannon(): Unit = reset(Pieces.cannon, List(1, 7), List(2, 7), 7)

  private def resetVehicle(): Unit = reset(Pieces.vehicle, List(0, 8), List(0, 9), 9)

  private def resetHorse(): Unit = reset(Pieces.horse, List(1, 7), List(0, 9), 9)

  private def resetElephant(): Unit = reset(Pieces.elephant, List(2, 6), List(0, 9), 9)

  private def resetGuard(): Unit = reset(Pieces.guard, List(3, 5), List(0, 9), 9)

  private def resetGeneral(): Unit = reset(Pieces.guard, List(4), List(0, 9), 9)

  private def reset(status: Int, line: List[Int], row: List[Int], f: Int => Boolean): Unit = {
    for {
      a <- line
      b <- row
    } checkerboard(a)(b) = Piece(f(b), status)
  }

  private def getLineStr(pos: Int): String = {
    val pieces = for (a <- 0 to 8) yield checkerboard(a)(pos)
    pieces.foldLeft("")(_ + _.toString)
  }

   def printChessBoardInConsole(): Unit = {
    val lines = for {
      a <- 0 to 9
    } yield getLineStr(a)
    lines foreach println
  }

}

case class Step(from: Pos, to: Pos, mover: String)

case class Pos(x: Int, y: Int) {

  def -(that: Pos): Int = Math.abs(x - that.x) + Math.abs(y - that.y)

  def mixed(that: Pos): Boolean = if (x - that.x != 0 && y - that.y != 0) true else false

  def distance(that: Pos): (Int, Int) = (x - that.x, y - that.y)

  def absDistance(that: Pos): (Int, Int) = (Math.abs(x - that.x), Math.abs(y - that.y))

}

case object MoveLaws {

  def soldierLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if ((x - y) != 1)
      false
    else {
      if (b) {
        //红色
        if (y.y > x.y) //后退
          false
        else if (y.y < x.y) {
          //前进
          if (f(y.x, y.y).isRed) false else true
        }
        else {
          //左右
          if (f(y.x, y.y).isRed) false
          else if (x.y >= 5) false else true
        }
      }
      else {
        //黑色
        if (y.y < x.y) //后退
          false
        else if (y.y > x.y) {
          //前进
          if (f(y.x, y.y).isBlack) false else true
        } else {
          //左右
          if (f(y.x, y.y).isBlack) false
          else if (x.y >= 5) true else false
        }
      }

    }

  }

  def cannonLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if ((x - y) == 0) //不动
      false
    else if (f(y.x, y.y).isRed == b) false
    else if (f(y.x, y.y).isBlack == (!b)) false
    else if (x mixed y) false
    else {
      //没有吃自己,也没有斜着走
      if (f(y.x, y.y).isEmpty) true //正常移动
      else {
        //吃子
        val (xd, yd) = y distance x
        if (xd == 0) {
          //竖直移动
          val range = if (yd > 0) x.y + 1 until y.y else y.y + 1 until x.y
          val pieces = for (i <- range) yield f(x.x, i)
          pieces.exists(!_.isEmpty) //中间是否有 《山》
        } else {
          //左右移动
          val range = if (xd > 0) x.x + 1 until y.x else y.x + 1 until x.x
          val pieces = for (i <- range) yield f(i, x.y)
          pieces.exists(!_.isEmpty)
        }
      }

    }
  }

  def vehicleLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if ((x - y) == 0) //不动
      false
    else if (f(y.x, y.y).isRed == b) false
    else if (f(y.x, y.y).isBlack == (!b)) false //吃自己
    else if (x mixed y) false //不能斜着走
    else true
  }

  def horseLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if ((x - y) != 3)
      false
    else if (f(y.x, y.y).isRed == b) false
    else if (f(y.x, y.y).isBlack == (!b)) false
    else {
      y distance x match {
        case (2, 1) | (2, -1) => f(x.x + 1, x.y).isEmpty
        case (-2, 1) | (-2, -1) => f(x.x - 1, x.y).isEmpty
        case (1, 2) | (-1, 2) => f(x.x, x.y + 1).isEmpty
        case (1, -2) | (-1, -2) => f(x.x, x.y - 1).isEmpty
        case _ => false //0,3 错误走法
      }
    }

  }


  def elephantLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if (b && y.y <= 4) //相不允许过河
      false
    else if (!b && y.y >= 5)
      false
    else if (f(y.x, y.y).isRed == b) false
    else if (f(y.x, y.y).isBlack == (!b)) false
    else y distance x match {
      case (2, 2) => f(x.x + 1, x.y + 1).isEmpty
      case (2, -2) => f(x.x + 1, x.y - 1).isEmpty
      case (-2, 2) => f(x.x - 1, x.y + 1).isEmpty
      case (-2, -2) => f(x.x - 1, x.y - 1).isEmpty
      case _ => false
    }
  }

  def guardLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if (y.x < 3 || y.x > 5) //左右越界
      false
    else if (b && y.y <= 6)
      false
    else if (!b && y.y >= 3)
      false
    else if (f(y.x, y.y).isRed == b) false
    else if (f(y.x, y.y).isBlack == (!b)) false
    else {
      y distance x match {
        case (1, -1) | (1, 1) | (-1, 1) | (-1, -1) => true
        case _ => false
      }
    }
  }

  def generalLaw(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    if (y.x < 3 || y.x > 5) //左右越界
      false
    else if (b && y.y <= 6) //上下越界
      false
    else if (!b && y.y >= 3)
      false
    else if (f(y.x, y.y).isRed == b) false //吃自己
    else if (f(y.x, y.y).isBlack == (!b)) false
    else (y - x) == 1
  }

  val laws: Map[Int, (Pos, Pos, Boolean, (Int, Int) => Pie) => Boolean] = Map(
    0 -> soldierLaw _,
    1 -> cannonLaw _,
    2 -> vehicleLaw _,
    3 -> horseLaw _,
    4 -> elephantLaw _,
    5 -> guardLaw _,
    6 -> generalLaw _
  )
}


abstract class Pie(chess: Boolean) {
  def isRed: Boolean

  def isEmpty: Boolean

  def isBlack: Boolean
}

case object Blank extends Pie(false) {
  override def toString: String = "X"

  override def isRed: Boolean = false

  override def isBlack: Boolean = false

  override def isEmpty: Boolean = true
}

case class Piece(red: Boolean, status: Int) extends Pie(true) {
  override def toString: String = status.toString

  override def isRed: Boolean = red

  override def isBlack: Boolean = !red

  override def isEmpty: Boolean = false
}

case object Pieces {
  val soldier = 0
  val cannon = 1
  val vehicle = 2
  val horse = 3
  val elephant = 4
  val guard = 5
  val general = 6
}

