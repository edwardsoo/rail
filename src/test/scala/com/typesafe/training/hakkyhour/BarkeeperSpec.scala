/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import scala.concurrent.duration.DurationInt

class BarkeeperSpec extends BaseSpec("barkeeper") {

  import Barkeeper._

  "Sending PrepareDrink to Barkeeper" should {
    "result in sending a DrinkPrepared response after prepareDrinkDuration" in {
      val barkeeper = system.actorOf(Barkeeper.props(100 milliseconds, 100))
      within(50 milliseconds, 1000 milliseconds) { // busy is very inaccurate, so we relax the timing constraints
        barkeeper ! PrepareDrink(Drink.Akkarita, system.deadLetters)
        expectMsg(DrinkPrepared(Drink.Akkarita, system.deadLetters))
      }
    }
    "result in sending a DrinkPrepared response with a random Drink for an inaccurate one" in {
      val accuracy = 50
      val runs = 1000
      val barkeeper = system.actorOf(Barkeeper.props(0 milliseconds, accuracy))
      val guest = system.deadLetters
      var drinks = List.empty[Drink]
      for (_ <- 1 to runs) {
        barkeeper ! PrepareDrink(Drink.Akkarita, guest)
        drinks +:= expectMsgPF() { case DrinkPrepared(drink, `guest`) => drink }
      }
      val expectedCount = runs * accuracy / 100
      val variation = expectedCount / 10
      drinks count (_ == Drink.Akkarita) shouldEqual expectedCount +- variation
    }
  }
}
