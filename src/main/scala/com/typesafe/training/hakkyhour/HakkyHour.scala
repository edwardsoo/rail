/*
 * Copyright ������ 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.ActorRef
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.FiniteDuration
import scala.collection.mutable.{ Map, Set }
import akka.actor.PoisonPill
import akka.actor.Terminated

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink, isStubborn: Boolean)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
  case object NoMoreDrinks
  def props(maxDrinkCount: Int): Props =
    Props(new HakkyHour(maxDrinkCount))
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging {

  log.debug(s"Hakky Hour is open!, Max ${maxDrinkCount} drinks per guest.")

  val guestDrinkCount: Map[ActorRef, Int] = Map.empty withDefaultValue 0
  val enoughDrinkGuest: Set[ActorRef] = Set.empty

  val finishDuration =
    FiniteDuration(
      context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
      MILLISECONDS)
  val prepareDuration =
    FiniteDuration(
      context.system.settings.config.getDuration("hakky-hour.barkeeper.prepare-drink-duration", MILLISECONDS),
      MILLISECONDS)

  val barkeeper = createBarkeeper()
  val waiter = createWaiter(self)

  override def receive: Receive = {
    case HakkyHour.CreateGuest(drink, isStubborn) => {
      val guest = context.actorOf(Guest.props(waiter, drink, finishDuration, isStubborn))
      context watch guest
    }
    case HakkyHour.ApproveDrink(drink, guest) =>
      if (guestDrinkCount(guest) >= maxDrinkCount && !enoughDrinkGuest.contains(guest)) {
        log.info(s"Sorry, ${guest}, but we won't serve you more than ${maxDrinkCount} drinks!")
        enoughDrinkGuest += guest
        guest ! HakkyHour.NoMoreDrinks
      } else if (enoughDrinkGuest contains guest) {
        log.debug("Enough, get out of here!")
        guest ! PoisonPill
      } else {
        log.debug("drink approved")
        guestDrinkCount(guest) += 1
        barkeeper forward Barkeeper.PrepareDrink(drink, guest)
      }
    case Terminated(guest) => log.info(s"Thanks, ${guest}, for being our guest!")
  }

  def createWaiter(hakkyHour: ActorRef): ActorRef =
    context.actorOf(Waiter.props(hakkyHour), "waiter")

  def createBarkeeper(): ActorRef =
    context.actorOf(Barkeeper.props(prepareDuration), "barkeeper")
}
