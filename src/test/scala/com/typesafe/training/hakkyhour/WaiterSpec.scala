/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.testkit.EventFilter

class WaiterSpec extends BaseSpec("waiter") {

  "Sending ServeDrink to Waiter" should {
    "result in sending ApproveDrink to HakkyHour" in {
      val hakkyHour = testActor
      val waiter = system.actorOf(Waiter.props(testActor, system.deadLetters, Int.MaxValue))
      waiter ! Waiter.ServeDrink(Drink.Akkarita)
      expectMsg(HakkyHour.ApproveDrink(Drink.Akkarita, hakkyHour))
    }
  }

  "Sending Complaint to Waiter" should {
    "result in sending PrepareDrink to Barkeeper" in {
      val waiter = system.actorOf(Waiter.props(system.deadLetters, testActor, 1))
      waiter ! Waiter.Complaint(Drink.Akkarita)
      expectMsg(Barkeeper.PrepareDrink(Drink.Akkarita, testActor))
    }
    "result in a FrustratedException if maxComplaintCount exceeded" in {
      val waiter = system.actorOf(Waiter.props(system.deadLetters, system.deadLetters, 0))
      EventFilter[Waiter.FrustratedException](occurrences = 1) intercept {
        waiter ! Waiter.Complaint(Drink.Akkarita)
      }
    }
  }
}
