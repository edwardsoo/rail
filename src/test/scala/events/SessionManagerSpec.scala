package events

import com.typesafe.training.hakkyhour.BaseSpec
import events.SessionManager.Tick
import akka.actor.Props

//@Ignore
class SessionManagerSpec extends BaseSpec("events") {
  "Creating SessionManager" should {

    "Schedule Tick messages to be sent" in {

      system.actorOf(Props(new SessionManager {
        override def receive = {
          case Tick => testActor ! Tick
        }
      }))

      expectMsg(Tick)
    }
  }
}
