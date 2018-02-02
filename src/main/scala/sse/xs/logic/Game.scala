package sse.xs.logic

import java.util.Scanner

import sse.xs.msg.Message
import sse.xs.msg.data.{GameRequest, MoveRequest, TurnChangeMsg}
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
  @volatile var count = 0
  //0未开始 1游戏中 2结束

  val sender: Sender = room.sender
  val checkerboard: Array[Array[Pie]] = new Array[Array[Pie]](9) map (x => new Array[Pie](10))
  val f: (Int, Int) => Pie = (a, b) => checkerboard(a)(b)

  implicit def intToF2(status: Int): (Pos, Pos) => Boolean = (x, y) => MoveLaws.laws(status)(x, y, f)

  def handleGameMessage(request: Message[_]): Unit = getMessage(request) filter {
    e =>
      e._1 foreach (x => sender.sendMessageSync(e._2.toString, x))
      e._3
  } onSuccess {
    case e => println("board get changed!")
      val state = inDangerOrDead
      val turnChange = new TurnChangeMsg(state)
      val m = createTurnMessage(turnChange)
      List(room.red, room.black) foreach (x => sender.sendMessageSync(m.toString, x))
      if(state==3||state==4)
        this.state = 2
  }

  private def createTurnMessage(turn: TurnChangeMsg) = {
    val msg = new Message[TurnChangeMsg]
    msg.`type` = Message.TYPE_TURN_CHANGE
    msg.data = turn
    msg
  }


  private def redBossLocation(): Option[(Int, Int)] = {
    val list = for {
      i <- 3 to 5
      j <- 7 to 9
      if checkerboard(i)(j).status == 6
    } yield (i, j)
    if (list.isEmpty) None
    else Some(list(0))
  }

  private def blackBossLocation(): Option[(Int, Int)] = {
    val list = for {
      i <- 3 to 5
      j <- 0 to 2
      if checkerboard(i)(j).status == 6
    } yield (i, j)
    if (list.isEmpty) None
    else Some(list(0))
  }

  //1 红色将军 2 黑色将军 3 红色胜利 4 黑色胜利 5 无事发生

  //redTurn已经改变，指示着下一个下棋的人了
  private def inDangerOrDead: Int = {
    val r = redBossLocation()
    val b = blackBossLocation()
    if (r.isEmpty)
      4
    else if (b.isEmpty)
      3
    else {
      val list = for (i <- r.get._2 - 1 to b.get._2 + 1) yield checkerboard(r.get._1)(i)
      if (r.get._1 == b.get._1 && !list.exists(_ != Blank)) {
        //在同一直线上并且两者间没有障碍物
        if (redTurn) 3 else 4
      }
      else {
        val bossPos = if (redTurn) r.get else b.get
        val list = for {
          i <- 0 to 8
          j <- 0 to 9
          if !checkerboard(i)(j).isEmpty && checkerboard(i)(j).isRed != redTurn //正确的颜色
          if canMove(Pos(i, j), Pos(bossPos._1, bossPos._2), checkerboard(i)(j).status)
        } yield 1
        if (list.isEmpty)
        //没有将军
          if (redTurn) 5 else 6
        else {
          if (redTurn) 2 else 1
        }
      }
    }
  }

  def getMessage(request: Message[_]): Future[(List[String], Message[_], Boolean)] = request.`type` match {
    case Message.TYPE_GAME_REQUEST =>
      val m = request.asInstanceOf[Message[GameRequest]]
      if (m.data.`type` == 1) startMessage(m) else endMessage(m)
    case Message.TYPE_MOVE_REQUEST => moveMessage(request.asInstanceOf[Message[MoveRequest]])
  }

  @deprecated
  private def moveForTest(from: Pos, to: Pos): Unit = {
    checkerboard(to.x)(to.y) = checkerboard(from.x)(from.y)
    checkerboard(from.x)(from.y) = Blank
    redTurn = !redTurn
  }

  def startMessage(request: Message[GameRequest]): Future[(List[String], Message[GameResponse], Boolean)] = {
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
        (List(request.key), startFailedMessage(), false)
      }
      else {
        state = 1
        (List(room.red, room.black), startSuccessMessage(), false)
      }
    }
  }

  def endMessage(request: Message[GameRequest]): Future[(List[String], Message[GameResponse], Boolean)] = {
    null
  }

  def moveMessage(request: Message[MoveRequest]): Future[(List[String], Message[MoveResponse], Boolean)] = {
    Future {
      val isRed: Boolean = request.key == room.red
      if (isRed == redTurn) {
        //我的回合
        val from = Pos(request.data.fromX, request.data.fromY)
        val to = Pos(request.data.toX, request.data.toY)
        moveStep(Step(from, to, request.key))
      } else {
        //不是我的回合
        (List(request.key), failMoveMessage("不是你的回合！"), false)
      }
    }
  }

  private def moveStep(step: Step): (List[String], Message[MoveResponse], Boolean) = {
    val isRed = step.mover == room.red
    if (checkerboard(step.from.y)(step.from.x) == Blank) {
      //空子
      val msg = failMoveMessage()
      (List(step.mover), msg, false)
    } else {
      val piece = checkerboard(step.from.x)(step.from.y).asInstanceOf[Piece]
      if (piece.red == isRed) {
        //颜色对了 走的是自己的棋子


        if (canMove(step.from, step.to, piece.status)) {
          actualMove(step.from, step.to)
          (List(room.red, room.black), successMoveResponse(step.from, step.to), true)
        } else {
          val msg = failMoveMessage()
          (List(step.mover), msg, false)
        }
      } else {
        //棋子颜色不对
        val msg = failMoveMessage()
        (List(step.mover), msg, false)
      }
    }
  }

  private def canMove(f: Pos, t: Pos, law: (Pos, Pos) => Boolean) = law(f, t)

  private def actualMove(from: Pos, to: Pos): Unit = {
    checkerboard(to.x)(to.y) = checkerboard(from.x)(from.y)
    checkerboard(from.x)(from.y) = Blank
    redTurn = !redTurn
  }

  private def successMoveResponse(f: Pos, t: Pos): Message[MoveResponse] = {
    val message = new Message[MoveResponse]
    message.`type` = Message.TYPE_MOVE_RESPONSE
    val data = new MoveResponse
    data.success = true
    count += 1
    data.count = count
    data.fromX = f.x
    data.fromY = f.y
    data.toX = t.x
    data.toY = t.y
    message.data = data
    message
  }

  private def failMoveMessage(info: String = null): Message[MoveResponse] = {
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

  private def resetGeneral(): Unit = reset(Pieces.general, List(4), List(0, 9), 9)

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
    println("##########################")
    val lines = for {
      a <- 0 to 9
    } yield getLineStr(a)
    lines foreach println
    println("##########################")
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

  //检查是否吃了自己
  private def checkSelfEat(x: Pos, y: Pos, b: Boolean, f: (Int, Int) => Pie): Boolean = {
    val piece = f(y.x, y.y)
    if (piece.isEmpty) false //目标地点没子，不会吃自己
    else {
      if (piece.isRed == b)
        true
      else if (piece.isBlack == (!b))
        true
      else false
    }
  }

  def soldierLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if ((x - y) != 1)
      false
    else if (checkSelfEat(x, y, b, f)) false
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

  def cannonLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if ((x - y) == 0) //不动
      false
    else if (checkSelfEat(x, y, b, f)) false
    else if (x mixed y) false
    else {
      val target = f(y.x, y.y)
      val count = if (target.isEmpty) 0 else 1
      val (xd, yd) = y distance x
      val pieces = if (xd == 0) {
        //竖直移动
        val range = if (yd > 0) x.y + 1 until y.y else y.y + 1 until x.y
        for (i <- range) yield f(x.x, i)
      } else {
        //左右移动
        val range = if (xd > 0) x.x + 1 until y.x else y.x + 1 until x.x
        for (i <- range) yield f(i, x.y)
      }
      pieces.count(_ != Blank) == count
    }
  }

  def vehicleLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed

    if ((x - y) == 0) //不动
      false
    else if (checkSelfEat(x, y, b, f)) false
    else if (x mixed y) false //不能斜着走
    else {
      val (xd, yd) = y distance x
      val pieces = if (xd == 0) {
        //竖直移动
        val range = if (yd > 0) x.y + 1 until y.y else y.y + 1 until x.y
        for (i <- range) yield f(x.x, i)
      } else {
        //左右移动
        val range = if (xd > 0) x.x + 1 until y.x else y.x + 1 until x.x
        for (i <- range) yield f(i, x.y)
      }
      !pieces.exists(_ != Blank) //不允许跨过其他单位
    }

  }


  def horseLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if ((x - y) != 3)
      false
    else if (checkSelfEat(x, y, b, f)) false
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


  def elephantLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if (b && y.y <= 4) //相不允许过河
      false
    else if (!b && y.y >= 5)
      false
    else if (checkSelfEat(x, y, b, f)) false
    else y distance x match {
      case (2, 2) => f(x.x + 1, x.y + 1).isEmpty
      case (2, -2) => f(x.x + 1, x.y - 1).isEmpty
      case (-2, 2) => f(x.x - 1, x.y + 1).isEmpty
      case (-2, -2) => f(x.x - 1, x.y - 1).isEmpty
      case _ => false
    }
  }

  /**
    *
    * @param x
    * @param y
    * @param f
    * @return
    */
  def guardLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if (y.x < 3 || y.x > 5) //左右越界
      false
    else if (b && y.y <= 6)
      false
    else if (!b && y.y >= 3)
      false
    else if (checkSelfEat(x, y, b, f)) false
    else {
      y distance x match {
        case (1, -1) | (1, 1) | (-1, 1) | (-1, -1) => true
        case _ => false
      }
    }
  }

  def generalLaw(x: Pos, y: Pos, f: (Int, Int) => Pie): Boolean = {
    val b = f(x.x, x.y).isRed
    if (y.x < 3 || y.x > 5) //左右越界
      false
    else if (b && y.y <= 6) //上下越界
      false
    else if (!b && y.y >= 3)
      false
    else if (checkSelfEat(x, y, b, f)) false
    else (y - x) == 1
  }

  val laws: Map[Int, (Pos, Pos, (Int, Int) => Pie) => Boolean] = Map(
    0 -> soldierLaw _,
    1 -> cannonLaw _,
    2 -> vehicleLaw _,
    3 -> horseLaw _,
    4 -> elephantLaw _,
    5 -> guardLaw _,
    6 -> generalLaw _
  )
}

object Game extends App {
  val game = new Game(new Room(null, 3, null, null, null))
  game.resetPieces()
  game.printChessBoardInConsole()
  var end = false
  while (!end) {
    println("输入移动：")
    val scanner = new Scanner(System.in)
    val str = scanner.nextLine()
    val f: (Int, Int) => Pie = (a, b) => game.checkerboard(a)(b)

    implicit def toPos(a: String): Pos = {
      val p = a.split(",")
      Pos(p(0).toInt, p(1).toInt)
    }

    if (str == "end") {
      end = true
    } else {
      val poss = str.split("#")
      if (poss.size == 1) {
        val piece = f(poss(0).x, poss(0).y)
        println(piece.info)
      } else {
        val piece = game.checkerboard(poss(0).x)(poss(0).y).asInstanceOf[Piece]

        implicit def intToF3(status: Int): (Pos, Pos) => Boolean = (x, y) => MoveLaws.laws(status)(x, y, f)

        if (game.canMove(poss(0), poss(1), piece.status)) {
          println("合法移动！")
          game.moveForTest(poss(0), poss(1))
          game.printChessBoardInConsole()
        } else {
          println("非法移动！")
        }
      }
    }
  }
  println("game ended")
}

abstract class Pie(chess: Boolean) {
  def isRed: Boolean

  def status: Int

  def info: String

  def isEmpty: Boolean

  def isBlack: Boolean
}

case object Blank extends Pie(false) {
  override def toString: String = "X"

  override def isRed: Boolean = false

  override def isBlack: Boolean = false

  override def isEmpty: Boolean = true

  override def info: String = "Blank"

  override def status: Int = -1
}

case class Piece(red: Boolean, status: Int) extends Pie(true) {
  override def toString: String = status.toString

  override def isRed: Boolean = red

  override def isBlack: Boolean = !red

  override def isEmpty: Boolean = false

  override def info: String = "color:" + (if (isRed) "red" else "black") + "state:" + status
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

