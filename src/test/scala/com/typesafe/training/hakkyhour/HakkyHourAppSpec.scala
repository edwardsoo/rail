/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.ActorDSL._
import akka.testkit.EventFilter
import akka.util.Timeout
import scala.concurrent.duration.DurationInt

class HakkyHourAppSpec extends BaseSpec("hakky-hour-app") {

  import HakkyHourApp._

  implicit val statusTimeout = 100 milliseconds: Timeout

  "Calling argsToOpts" should {
    "return the correct opts for the given args" in {
      argsToOpts(List("a=1", "b", "-Dc=2")) shouldEqual Map("a" -> "1", "-Dc" -> "2")
    }
  }

  "Calling applySystemProperties" should {
    "apply the system properties for the given opts" in {
      System.setProperty("c", "")
      applySystemProperties(Map("a" -> "1", "-Dc" -> "2"))
      System.getProperty("c") shouldEqual "2"
    }
  }

  "Creating CommandLineApp" should {
    """result in creating a top-level actor named "hakky-hour"""" in {
      new HakkyHourApp(system)
      expectActor("/user/hakky-hour")
    }
  }

  "Calling handleGuestCommand" should {
    "result in sending CreateGuest to HakkyHour count number of times" in {
      val app =
        new HakkyHourApp(system) {
          override def createHakkyHour() = testActor
        }
      app.createGuest(2, Drink.Akkarita, false, Int.MaxValue)
      val createGuest = HakkyHour.CreateGuest(Drink.Akkarita, false, Int.MaxValue)
      receiveN(2) shouldEqual List.fill(2)(createGuest)
    }
  }

  "Calling handleStatusCommand" should {
    "result in logging the AskTimeoutException at error for HakkyHour not responding" in {
      val app =
        new HakkyHourApp(system) {
          override def createHakkyHour() = testActor
        }
      EventFilter.error(pattern = ".*AskTimeoutException.*") intercept {
        app.getStatus()
      }
    }
    "result in logging the status at info" in {
      val app =
        new HakkyHourApp(system) {
          override def createHakkyHour() =
            actor(new Act { become { case HakkyHour.GetStatus => sender() ! HakkyHour.Status(42) } })
        }
      EventFilter.info(pattern = ".*42.*") intercept {
        app.getStatus()
      }
    }
  }
}
