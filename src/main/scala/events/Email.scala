package events

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

object Email {
  def props = Props(classOf[Email])
  case class Send(msg: String)
}
class Email extends Actor with ActorLogging {
  import Email._

  def receive = {
    case Send(msg) => log.info(msg)
  }
}