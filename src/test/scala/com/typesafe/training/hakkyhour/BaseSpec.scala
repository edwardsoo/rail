/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ ActorIdentity, ActorRef, ActorSystem, Identify }
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import scala.concurrent.duration.{ DurationInt, FiniteDuration }

abstract class BaseSpec(name: String) extends TestKit(ActorSystem(s"$name-spec")) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll(): Unit =
    shutdown(system)

  def expectActor(path: String, max: FiniteDuration = 5 seconds): ActorRef =
    within(max) {
      var actor = null: ActorRef
      awaitAssert {
        system.actorSelection(path) ! Identify(path)
        expectMsgPF(250 milliseconds) { case ActorIdentity(`path`, Some(a)) => actor = a }
      }
      actor
    }
}
