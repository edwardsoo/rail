/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

class WaiterSpec extends BaseSpec("waiter") {

  "Sending ServeDrink to Waiter" should {
    "result in sending a DrinkServed response to the sender" in {
      val waiter = system.actorOf(Waiter.props)
      waiter ! Waiter.ServeDrink(Drink.Akkarita)
      expectMsg(Waiter.DrinkServed(Drink.Akkarita))
    }
  }
}
