/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, ActorIdentity, Identify }
import akka.actor.ActorDSL._
import akka.testkit.EventFilter
import scala.concurrent.duration.DurationInt
import com.typesafe.training.hakkyhour.SettingsActor

object HakkyHourSpec {

  trait BarkeeperNoRouter {
    this: HakkyHour with SettingsActor =>

    override def createBarkeeper() =
      context.actorOf(Barkeeper.props(settings.barkeeperPrepareDrinkDuration, settings.barkeeperAccuracy), "barkeeper")
  }
}

class HakkyHourSpec extends BaseSpec("hakky-hour") {

  import HakkyHourSpec._

  "Creating HakkyHour" should {
    "result in logging a status message at debug" in {
      EventFilter.debug(pattern = ".*open.*", occurrences = 1) intercept {
        actor(new HakkyHour(Int.MaxValue) with BarkeeperNoRouter)
      }
    }
    """result in creating a child actor with name "barkeeper"""" in {
      actor("create-barkeeper")(new HakkyHour(Int.MaxValue) with BarkeeperNoRouter)
      expectActor("/user/create-barkeeper/barkeeper")
    }
    """result in creating a child actor with name "waiter"""" in {
      actor("create-waiter")(new HakkyHour(Int.MaxValue) with BarkeeperNoRouter)
      expectActor("/user/create-waiter/waiter")
    }
  }

  "Sending CreateGuest to HakkyHour" should {
    "result in creating a Guest" in {
      val hakkyHour = actor("create-guest")(new HakkyHour(Int.MaxValue) with BarkeeperNoRouter)
      hakkyHour ! HakkyHour.CreateGuest(Drink.Akkarita, false, Int.MaxValue)
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
      val hakkyHour = actor(new HakkyHour(0) with BarkeeperNoRouter)
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, testActor)
      expectMsg(HakkyHour.NoMoreDrinks)
    }
    "result in sending a PoisonPill response if maxDrinkCount exceeded" in {
      val hakkyHour = actor(new HakkyHour(0) with BarkeeperNoRouter)
      val guest = actor(new Act { become { case message => testActor forward message } })
      watch(guest)
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, guest)
      expectMsg(HakkyHour.NoMoreDrinks)
      hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, guest)
      expectTerminated(guest)
    }
  }

  "On termination of Guest HakkyHour" should {
    "remove it from the number-of-drinks-per-guest bookkeeping" in {
      val hakkyHour =
        actor(new HakkyHour(Int.MaxValue) {
          override def createBarkeeper() = testActor
        })
      hakkyHour ! HakkyHour.CreateGuest(Drink.Akkarita, false, Int.MaxValue)
      val guest = expectMsgPF() { case Barkeeper.PrepareDrink(Drink.Akkarita, guest) => guest }
      watch(guest)
      system.stop(guest)
      expectTerminated(guest)
      within(2 seconds) {
        awaitAssert {
          hakkyHour ! HakkyHour.ApproveDrink(Drink.Akkarita, guest)
          expectMsgPF(100 milliseconds) { case Barkeeper.PrepareDrink(Drink.Akkarita, `guest`) => () }
        }
      }
    }
  }

  "On failure of Guest HakkyHour" should {
    "stop it" in {
      val hakkyHour =
        actor(new HakkyHour(Int.MaxValue) {
          override def createBarkeeper() = testActor
        })
      hakkyHour ! HakkyHour.CreateGuest(Drink.Akkarita, false, 0)
      val guest = expectMsgPF() { case Barkeeper.PrepareDrink(Drink.Akkarita, guest) => guest }
      watch(guest)
      guest ! Waiter.DrinkServed(Drink.Akkarita)
      expectTerminated(guest)
    }
  }

  "On failure of Waiter HakkyHour" should {
    "restart it and resend PrepareDrink to Barkeeper" in {
      actor("resend-prepare-drink")(new HakkyHour(Int.MaxValue) {
        override def createBarkeeper() = testActor
        override def createWaiter() =
          actor(context, "waiter")(new Act {
            become { case _ => throw new Waiter.FrustratedException(Drink.Akkarita, system.deadLetters) }
          })
      })
      val waiter = expectActor("/user/resend-prepare-drink/waiter")
      waiter ! "blow-up"
      expectMsg(Barkeeper.PrepareDrink(Drink.Akkarita, system.deadLetters))
    }
  }

  "Sending GetStatus to HakkyHour" should {
    "result in a Status response" in {
      val hakkyHour = actor(new HakkyHour(Int.MaxValue) with BarkeeperNoRouter)
      hakkyHour ! HakkyHour.GetStatus
      expectMsg(HakkyHour.Status(0))
    }
  }
}
