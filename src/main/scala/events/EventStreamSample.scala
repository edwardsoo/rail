package events

import akka.actor.ActorSystem
import scala.collection.mutable.Map
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.event.Logging

object EventStreamSample extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val system = ActorSystem("session-system")
  val log = Logging(system, getClass.getName)
  val sessionMananger = system.actorOf(SessionManager.props(), "session-manager")
}
