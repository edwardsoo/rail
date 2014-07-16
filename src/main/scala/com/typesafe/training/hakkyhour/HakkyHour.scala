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
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.SupervisorStrategy
import akka.actor.OneForOneStrategy
import akka.routing.FromConfig

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink, isStubborn: Boolean, maxDrinkCount: Int)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
  case object NoMoreDrinks
  def props(maxDrinkCount: Int): Props =
    Props(new HakkyHour(maxDrinkCount))
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging {

  log.debug(s"Hakky Hour is open!, Max ${maxDrinkCount} drinks per guest.")

  val guestDrinkCount: Map[ActorRef, Int] = Map.empty withDefaultValue 0

  val finishDuration =
    FiniteDuration(
      context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
      MILLISECONDS)
  val barkeeperPrepareDrinkDuration =
    FiniteDuration(
      context.system.settings.config.getDuration("hakky-hour.barkeeper.prepare-drink-duration", MILLISECONDS),
      MILLISECONDS)
  val maxComplaint =
    context.system.settings.config.getInt("hakky-hour.waiter.max-complaint-count")
  val barkeeperAccuracy =
    context.system.settings.config.getInt("hakky-hour.barkeeper.accuracy")

  val barkeeper = createBarkeeper()
  val waiter = createWaiter()

  override val supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case Guest.DrunkException => Stop
      case Waiter.FrustratedException(drink, guest) =>
        barkeeper.tell(Barkeeper.PrepareDrink(drink, guest), sender)
        Restart
    }
    OneForOneStrategy()(decider orElse super.supervisorStrategy.decider)
  }

  override def receive: Receive = {
    case HakkyHour.CreateGuest(drink, isStubborn, maxDrinkCount) =>
      val guest = context.actorOf(Guest.props(waiter, drink, finishDuration, isStubborn, maxDrinkCount))
      context watch guest
    case HakkyHour.ApproveDrink(drink, guest) =>
      if (guestDrinkCount(guest) == maxDrinkCount) {
        log.info(s"Sorry, ${guest.path.name}, but we won't serve you more than ${maxDrinkCount} drinks!")
        guestDrinkCount(guest) += 1
        guest ! HakkyHour.NoMoreDrinks
      } else if (guestDrinkCount(guest) > maxDrinkCount) {
        // log.debug("Enough, get out of here!")
        guest ! PoisonPill
      } else {
        // log.debug("drink approved")
        guestDrinkCount(guest) += 1
        barkeeper forward Barkeeper.PrepareDrink(drink, guest)
      }
    case Terminated(guest) =>
      log.info(s"Thanks, ${guest.path.name}, for being our guest!")
      guestDrinkCount - guest
  }

  def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(self, barkeeper, maxComplaint), "waiter")

  def createBarkeeper(): ActorRef =
    //context.actorOf(Barkeeper.props(barkeeperPrepareDrinkDuration, barkeeperAccuracy), "barkeeper")
    context.actorOf(
      Barkeeper.props(barkeeperPrepareDrinkDuration, barkeeperAccuracy).withRouter(FromConfig()),
      "barkeeper")
}
