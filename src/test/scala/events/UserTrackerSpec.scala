package events

import com.typesafe.training.hakkyhour.BaseSpec
import events.UserTracker.{History, Inactive}
import akka.actor.{Actor, Props}

class UserTrackerSpec extends BaseSpec("events") {

  "Sending Inactive Message" should {

    "send an Inactive Message to the Parent" in {
      class Dummy extends Actor {
        val userTracker = context.actorOf(UserTracker.props(Session(5), system.deadLetters), "usertracker")
        def receive = {
          case message: Inactive => testActor forward message
        }
      }
      val dummy = system.actorOf(Props(new Dummy), "dummy")
      dummy ! Inactive(Session(5))

      expectMsg(Inactive(Session(5)))
    }

    "send a history message to the Stats actor" in {
      val userTracker = system.actorOf(UserTracker.props(Session(5), testActor), "userTracker")
      userTracker ! Inactive(Session(5))
      expectMsg(History(List()))
    }

  }
}
