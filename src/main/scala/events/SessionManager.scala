package events

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Terminated
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.collection.mutable.Map
import akka.actor.Props

object SessionManager {
  case object Tick

  def props = Props(classOf[SessionManager])
}

class SessionManager extends Actor with ActorLogging {
  import context.dispatcher
  import SessionManager._

  val sessionMap: Map[Session, ActorRef] = Map.empty
  val stream = new EventStream(5)
  val stats = context.actorOf(Stats.props, "stats")

  log.info("session-manager started")
  println("session-manager started")

  override def preStart =
    context.system.scheduler.schedule(0 seconds, 1 seconds, self, Tick)

  def receive = {
    case Tick =>
      stream.tick foreach (req => (sessionMap get (req.session)) match {
        case None =>
          val tracker = context.actorOf(UserTracker.props(req.session, stats), s"${req.session.id}")
          sessionMap += (req.session -> tracker)
          tracker ! UserTracker.TrackRequest(req)
        case Some(tracker) => tracker ! UserTracker.TrackRequest(req)
      })
    case UserTracker.Inactive(session) =>
      if (!sessionMap.contains(session)) log.warning("session not in map")
      sessionMap -= session
  }

}