/**
  * Created by xusong on 2018/1/4.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class RNG(seed: Int) {
  def nextInt: (Int, RNG) = (seed, new RNG(seed + 1))
}

object RNG extends App {
  type Rand[A] = State[RNG, A]
  val int: Rand[Int] = State(_.nextInt)
  val random=for{
    x <- int
    y <- int
  } yield {
    println("x:"+x)
    println("y:"+y)
    x+y
  }
  random.run(new RNG(2))
}

case class State[S, +A](run: S => (A, S)) {

  def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
    val (a, s1) = run(s)
    f(a).run(s1)
  })

  def map[B](f: A => B): State[S, B] = {
    val function: S => (B, S) = s => {
      val (a, ss) = run(s)
      (f(a), ss)
    }
    State(function)
  }

}
