package events

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props

object Stats {
  def props = Props(classOf[Stats])
}

class Stats extends Actor with ActorLogging {
  def receive = {
    case UserTracker.History(history) =>
      log.info("got history")
      println("got history")
  }
}