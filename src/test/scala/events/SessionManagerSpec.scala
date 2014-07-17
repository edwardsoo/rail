package events

import com.typesafe.training.hakkyhour.BaseSpec
import events.SessionManager.Tick
import akka.actor.Props

//@Ignore
class SessionManagerSpec extends BaseSpec("events") {
  "SessionManager" should {

    "Schedules Tick messages to be sent" in {

      system.actorOf(Props(new SessionManager {
        override def receive = {
          case Tick => testActor ! Tick
        }
      }))

      expectMsg(Tick)
    }

    "remove the session from the session map when an inactive message is received" in {
      val sessionManager = system.actorOf(SessionManager.props())
      sessionManager
    }

  }

}
