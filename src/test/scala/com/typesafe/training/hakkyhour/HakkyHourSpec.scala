/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ ActorIdentity, Identify }
import akka.actor.ActorDSL._
import akka.testkit.EventFilter
import scala.concurrent.duration.DurationInt

class HakkyHourSpec extends BaseSpec("hakky-hour") {

  "Creating HakkyHour" should {
    "result in logging a status message at debug" in {
      EventFilter.debug(pattern = ".*open.*", occurrences = 1) intercept {
        system.actorOf(HakkyHour.props(Int.MaxValue))
      }
    }
    """result in creating a child actor with name "barkeeper"""" in {
      system.actorOf(HakkyHour.props(Int.MaxValue), "create-barkeeper")
      expectActor("/user/create-barkeeper/barkeeper")
    }
    """result in creating a child actor with name "waiter"""" in {
      system.actorOf(HakkyHour.props(Int.MaxValue), "create-waiter")
      expectActor("/user/create-waiter/waiter")
    }
  }

  "Sending CreateGuest to HakkyHour" should {
    "result in creating a Guest" in {
      val hakkyHour = system.actorOf(HakkyHour.props(Int.MaxValue), "create-guest")
      hakkyHour ! HakkyHour.CreateGuest(Drink.Akkarita, false)
      expectActor("/user/create-guest/$*")
    }
  }

  "Sending ApproveDrink to HakkyHour" should {
    "result in forwarding PrepareDrink to Barkeeper if maxDrinkCount not yet reached" in {
      val hakkyHour =
        actor(new HakkyHour(Int.MaxValue) {
          override def createBarkeeper() = testActor
        })
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, system.deadLetters)
      expectMsg(Barkeeper.PrepareDrink(Drink.Akkarita, system.deadLetters))
    }
    "result in sending a NoMoreDrinks response if maxDrinkCount reached" in {
      val hakkyHour = system.actorOf(HakkyHour.props(0))
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, testActor)
      expectMsg(HakkyHour.NoMoreDrinks)
    }
    "result in sending a PoisonPill response if maxDrinkCount exceeded" in {
      val hakkyHour = system.actorOf(HakkyHour.props(0))
      val guest = actor(new Act { become { case message => testActor forward message } })
      watch(guest)
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, guest)
      expectMsg(HakkyHour.NoMoreDrinks)
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, guest)
      expectTerminated(guest)
    }
  }
}
