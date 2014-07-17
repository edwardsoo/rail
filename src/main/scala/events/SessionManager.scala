package events

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Restart
import scala.collection.mutable


object SessionManager {
  case object Tick

  def props(sessionMap: mutable.Map[Session, ActorRef] = mutable.Map.empty) = Props(classOf[SessionManager], sessionMap)
}

class SessionManager(sessionMap: mutable.Map[Session, ActorRef] = mutable.Map.empty) extends Actor with ActorLogging {
  import context.dispatcher
  import SessionManager._

  override val supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case e: Exception =>
        email ! Email.Send(e.toString)
        Restart
    }
    OneForOneStrategy(5)(decider orElse super.supervisorStrategy.decider)
  }

  val stream = new EventStream(5)
  val email = context.actorOf(Email.props, "email")
  val stats = context.actorOf(Stats.props, "stats")
  context.watch(stats)

  log.info("session-manager started")

  override def preStart() =
    context.system.scheduler.schedule(0 seconds, 1 seconds, self, Tick)

  def receive = {
    case Tick =>
      stream.tick foreach (req => sessionMap get req.session match {
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