package events

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Cancellable
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.Props

object UserTracker {
  case class TrackRequest(req: Request)
  case class History(history: Seq[Request])
  case class Inactive(session: Session)

  def props(session: Session, stats: ActorRef) = Props(classOf[UserTracker], session, stats)
}

class UserTracker(session: Session, stats: ActorRef) extends Actor with ActorLogging {
  import UserTracker._
  import context.dispatcher

  var timer = context.system.scheduler.scheduleOnce(1 seconds, self, Inactive(session))
  var history = Seq[Request]()

  def receive = {
    case TrackRequest(req) =>
      log.info(s"received $req")
      println(s"received $req")
      history = history :+ req
      timer.cancel
      timer = context.system.scheduler.scheduleOnce(5 minutes, self, Inactive(session))
    case Inactive(_) =>
      stats ! History(history)
      context.parent ! Inactive(session)
      context.stop(self)
  }
}