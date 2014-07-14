/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ ActorIdentity, Identify }
import akka.testkit.EventFilter
import scala.concurrent.duration.DurationInt

class HakkyHourSpec extends BaseSpec("hakky-hour") {

  "Creating HakkyHour" should {
    "result in logging a status message at debug" in {
      EventFilter.debug(pattern = ".*open.*", occurrences = 1) intercept {
        system.actorOf(HakkyHour.props)
      }
    }
    """result in creating a child actor with name "waiter"""" in {
      system.actorOf(HakkyHour.props, "create-waiter")
      expectActor("/user/create-waiter/waiter")
    }
  }

  "Sending CreateGuest to HakkyHour" should {
    "result in creating a Guest" in {
      val hakkyHour = system.actorOf(HakkyHour.props, "create-guest")
      hakkyHour ! HakkyHour.CreateGuest(Drink.Akkarita)
      expectActor("/user/create-guest/$*")
    }
  }
}
