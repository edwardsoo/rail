/*
 * Copyright 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import scala.annotation.tailrec
import scala.collection.breakOut
import scala.io.StdIn
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.pattern.ask
import scala.concurrent.duration._
import akka.pattern.AskTimeoutException
import scala.util.Success
import scala.util.Failure
import akka.util.Timeout
import com.typesafe.training.hakkyhour.Settings

object HakkyHourApp {

  val Opt = """(\S+)=(\S+)""".r

  def main(args: Array[String]): Unit = {
    val opts = argsToOpts(args.toList)
    applySystemProperties(opts)
    val name = opts.getOrElse("name", "hakky-hour")

    val system = ActorSystem(s"$name-system")
    val hakkyHourApp = new HakkyHourApp(system)
    hakkyHourApp.run()
  }

  def argsToOpts(args: Seq[String]): Map[String, String] =
    args.collect { case Opt(key, value) => key -> value }(breakOut)

  def applySystemProperties(opts: Map[String, String]): Unit =
    for ((key, value) <- opts if key startsWith "-D")
      System.setProperty(key substring 2, value)
}

class HakkyHourApp(system: ActorSystem) extends Terminal {

  val log = Logging(system, getClass.getName)

  val hakkyHour = createHakkyHour()

  //  val myActor = system.actorOf(Props(new Actor with ActorLogging {
  //    hakkyHour ! "Nice bar!"
  //    override def receive = {
  //      case x => log.info(x.toString)
  //    }
  //  }))

  def run(): Unit = {
    log.warning(f"{} running%nEnter commands into the terminal, e.g. `q` or `quit`", getClass.getSimpleName)
    commandLoop()
    system.awaitTermination()
  }

  def createHakkyHour(): ActorRef = {
    system.actorOf(
      HakkyHour.props(Settings(system).maxDrinkCount),
      "hakky-hour")
  }

  @tailrec
  final def commandLoop(): Unit =
    Command(StdIn.readLine()) match {
      case Command.CreateGuest(count, drink, isStubborn, maxDrinkCount) =>
        createGuest(count, drink, isStubborn, maxDrinkCount)
        commandLoop()
      case Command.GetStatus =>
        getStatus()
        commandLoop()
      case Command.Quit =>
        system.shutdown()
      case Command.Unknown(command) =>
        log.warning("Unknown command {}!", command)
        commandLoop()
    }

  def createGuest(count: Int, drink: Drink, isStubborn: Boolean, maxDrinkCount: Int): Unit =
    (1 to count).foreach(_ => hakkyHour ! HakkyHour.CreateGuest(drink, isStubborn, maxDrinkCount))

  def getStatus(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val f = hakkyHour.ask(HakkyHour.Status)(Settings(system).statusTimeout).mapTo[HakkyHour.Status]
    f.onComplete {
      case Success(HakkyHour.Status(num)) => log.info(s"number of guests is $num")
      case Failure(e)                     => log.error(s"GetStatus error: $e")
    }
  }
}
