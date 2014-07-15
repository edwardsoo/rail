/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import scala.concurrent.duration.DurationInt

class BarkeeperSpec extends BaseSpec("barkeeper") {

  import Barkeeper._

  "Sending PrepareDrink to Barkeeper" should {
    "result in sending a DrinkPrepared response after prepareDrinkDuration" in {
      val barkeeper = system.actorOf(Barkeeper.props(100 milliseconds))
      within(50 milliseconds, 1000 milliseconds) { // busy is very inaccurate, so we relax the timing constraints
        barkeeper ! PrepareDrink(Drink.Akkarita, system.deadLetters)
        expectMsg(DrinkPrepared(Drink.Akkarita, system.deadLetters))
      }
    }
  }
}
