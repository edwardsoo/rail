/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.testkit.{ EventFilter, TestActorRef }
import scala.concurrent.duration.DurationInt
import scala.reflect.runtime.{ universe => ru }

class GuestSpec extends BaseSpec("guest") {

  "Sending DrinkServed to Guest" should {
    "result in increasing the drinkCount and logging a status message at debug" in {
      val guest = TestActorRef(new Guest(system.deadLetters, Drink.Akkarita, 100 milliseconds, false, Int.MaxValue))
      EventFilter.info(pattern = ".*[Ee]njoy.*", occurrences = 1) intercept {
        guest ! Waiter.DrinkServed(Drink.Akkarita)
      }
      guest.underlyingActor.drinkCount shouldEqual 1
    }
    "result in sending ServeDrink to Waiter after finishDrinkDuration" in {
      val guest = createGuest()
      within(50 milliseconds, 200 milliseconds) { // The timer is not extremely accurate, so we relax the timing constraints
        guest ! Waiter.DrinkServed(Drink.Akkarita)
        expectMsg(Waiter.ServeDrink(Drink.Akkarita))
      }
    }
    "result in sending Complaint to Waiter for a wrong drink" in {
      val guest = createGuest()
      guest ! Waiter.DrinkServed(Drink.PinaScalada)
      expectMsg(Waiter.Complaint(Drink.Akkarita))
    }
  }

  "Sending DrinkFinished to Guest" should {
    "result in sending ServeDrink to Waiter" in {
      val guest = createGuest()
      guest ! drinkFinished
      expectMsg(Waiter.ServeDrink(Drink.Akkarita))
    }
    "result in a DrunkException if maxDrinkCount exceeded" in {
      val guest = system.actorOf(Guest.props(system.deadLetters, Drink.Akkarita, 100 millis, false, -1))
      EventFilter[Guest.DrunkException.type](occurrences = 1) intercept {
        guest ! drinkFinished
      }
    }
  }

  "Sending NoMoreDrinks to Guest" should {
    "result in Guest stopping itself" in {
      val guest = createGuest()
      watch(guest)
      guest ! HakkyHour.NoMoreDrinks
      expectTerminated(guest)
    }
    "result in sending ServeDrink to Waiter for a stubborn Guest" in {
      val guest = createGuest(true)
      guest ! HakkyHour.NoMoreDrinks
      expectMsg(Waiter.ServeDrink(Drink.Akkarita))
    }
  }

  lazy val drinkFinished = { // This freaking crazy code is needed to access the private DrinkFinished case object
    val drinkFinishedSymbol = ru.typeOf[Guest.type].decl(ru.TermName("DrinkFinished")).asModule
    ru.runtimeMirror(getClass.getClassLoader).reflectModule(drinkFinishedSymbol).instance
  }

  def createGuest(isStubborn: Boolean = false, maxDrinkCount: Int = Int.MaxValue) = {
    val guest = system.actorOf(Guest.props(testActor, Drink.Akkarita, 100 milliseconds, isStubborn, maxDrinkCount))
    expectMsg(100 milliseconds, Waiter.ServeDrink(Drink.Akkarita)) // Creating Guest immediately sends Waiter.ServeDrink
    guest
  }
}
