/*
 * Copyright ������ 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.ActorRef
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.FiniteDuration

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink)
  def props: Props =
    Props(new HakkyHour)
}

class HakkyHour extends Actor with ActorLogging {

  log.debug("Hakky Hour is open!")
  val waiter = createWaiter
  val duration =
    context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", SECONDS)

  override def receive: Receive = {
    case HakkyHour.CreateGuest(drink) => context.actorOf(Guest.props(waiter, drink, FiniteDuration(duration, SECONDS)))
    case _                            => sender() ! "Welcome to Hakky Hour!"
  }

  def createWaiter(): ActorRef =
    context.actorOf(Waiter.props, "waiter")
}
