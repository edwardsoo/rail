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
      val guest = TestActorRef(new Guest(system.deadLetters, Drink.Akkarita, 100 milliseconds))
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
  }

  "Sending DrinkFinished to Guest" should {
    "result in sending ServeDrink to Waiter" in {
      val drinkFinished = { // This freaking crazy code is needed to access the private DrinkFinished case object
        val drinkFinishedSymbol = ru.typeOf[Guest.type].decl(ru.TermName("DrinkFinished")).asModule
        ru.runtimeMirror(getClass.getClassLoader).reflectModule(drinkFinishedSymbol).instance
      }
      val guest = createGuest()
      guest ! drinkFinished
      expectMsg(Waiter.ServeDrink(Drink.Akkarita))
    }
  }

  def createGuest() = {
    val guest = system.actorOf(Guest.props(testActor, Drink.Akkarita, 100 milliseconds))
    expectMsg(100 milliseconds, Waiter.ServeDrink(Drink.Akkarita)) // Creating Guest immediately sends Waiter.ServeDrink
    guest
  }
}
