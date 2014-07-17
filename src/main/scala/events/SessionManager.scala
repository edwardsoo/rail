package events

import akka.actor._
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy.Restart
import scala.Some
import akka.actor.OneForOneStrategy
import scala.collection.mutable

object SessionManager {
  case object Tick

  def props() = Props(classOf[SessionManager])
}

class SessionManager extends Actor with ActorLogging {
  import context.dispatcher
  import SessionManager._

  override val supervisorStrategy: SupervisorStrategy = {
    var nrRestarts = 0
    val decider: SupervisorStrategy.Decider = {
      case e: Exception =>
        nrRestarts += 1
        if (nrRestarts > 5) {
          email ! Email.Send(s"Stat actor has crashed $nrRestarts times")
        }
        Restart
    }
    OneForOneStrategy(5)(decider orElse super.supervisorStrategy.decider)
  }

  val sessionMap: mutable.Map[Session, ActorRef] = mutable.Map.empty
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