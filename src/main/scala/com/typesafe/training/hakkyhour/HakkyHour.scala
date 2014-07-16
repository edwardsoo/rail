/*
 * Copyright ������ 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.ActorRef
import scala.collection.mutable.Map
import akka.actor.PoisonPill
import akka.actor.Terminated
import akka.actor.SupervisorStrategy._
import akka.actor.SupervisorStrategy
import akka.actor.OneForOneStrategy
import akka.routing.FromConfig

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink, isStubborn: Boolean, maxDrinkCount: Int)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
  case object NoMoreDrinks
  case object GetStatus
  case class Status(guestCount: Int)
  def props(maxDrinkCount: Int): Props =
    Props(new HakkyHour(maxDrinkCount))
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging with SettingsActor {

  log.debug(s"Hakky Hour is open!, Max ${maxDrinkCount} drinks per guest.")

  val guestDrinkCount: Map[ActorRef, Int] = Map.empty withDefaultValue 0

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
      val guest = context.actorOf(Guest.props(waiter, drink, settings.finishDrinkDuration, isStubborn, maxDrinkCount))
      guestDrinkCount(guest) = 0
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
    case HakkyHour.GetStatus =>
      sender ! HakkyHour.Status(guestDrinkCount.size)
    case Terminated(guest) =>
      log.info(s"Thanks, ${guest.path.name}, for being our guest!")
      guestDrinkCount - guest
  }

  def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(self, barkeeper, settings.maxComplaint), "waiter")

  def createBarkeeper(): ActorRef =
    //context.actorOf(Barkeeper.props(barkeeperPrepareDrinkDuration, barkeeperAccuracy), "barkeeper")
    context.actorOf(
      Barkeeper.props(settings.barkeeperPrepareDrinkDuration, settings.barkeeperAccuracy).withRouter(FromConfig()),
      "barkeeper")
}
