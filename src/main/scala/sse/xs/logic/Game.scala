package sse.xs.logic

import sse.xs.msg.Message

/**
  * Created by xusong on 2018/1/6.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class Game(room: Room) {

  @volatile var redTurn = true

  val checkerboard: Array[Array[Pie]] = new Array[Array[Pie]](9) map (x => new Array[Pie](10))

  def start(): Unit = {
    redTurn = true
    resetPieces()

  }

  def nextStep(step: Step): Unit = {

  }

  def cleanPieces(pieces: Array[Pie]): Unit = {
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

  def resetSoldiers(): Unit = reset(0, List(0, 2, 4, 6, 8), List(3, 6), 6)

  def resetCannon(): Unit = reset(1, List(1, 7), List(2, 7), 7)

  def resetVehicle(): Unit = reset(2, List(0, 8), List(0, 9), 9)

  def resetHorse(): Unit = reset(3, List(1, 7), List(0, 9), 9)

  def resetElephant(): Unit = reset(4, List(2, 6), List(0, 9), 9)

  def resetGuard(): Unit = reset(5, List(3, 5), List(0, 9), 9)

  def resetGeneral(): Unit = reset(6, List(4), List(0, 9), 9)

  def reset(status: Int, line: List[Int], row: List[Int], f: Int => Boolean): Unit = {
    for {
      a <- line
      b <- row
    } checkerboard(a)(b) = Piece(f(b), status)
  }

  def getLineStr(pos: Int): String = {
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

object Game extends App {
  val game = new Game(null)
  game.start()
  game.printChessBoardInConsole()
}

case class Step(from: Pos, to: Pos)

case class Pos(x: Int, y: Int)

class Pie(chess: Boolean)

case object Blank extends Pie(false) {
  override def toString: String = "X"
}

case class Piece(red: Boolean, status: Int) extends Pie(true) {
  override def toString: String = status.toString
}

case object Pieces {
  val soldier = 0
  val cannon = 1
  val vehicle = 2
  val horse = 3
  val elephant = 4
  val guard = 5
  val genenral = 6
}

