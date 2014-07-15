/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

class WaiterSpec extends BaseSpec("waiter") {

  "Sending ServeDrink to Waiter" should {
    "result in sending ApproveDrink to HakkyHour" in {
      val hakkyHour = testActor
      val waiter = system.actorOf(Waiter.props(hakkyHour))
      waiter ! Waiter.ServeDrink(Drink.Akkarita)
      expectMsg(HakkyHour.ApproveDrink(Drink.Akkarita, hakkyHour))
    }
  }
}
