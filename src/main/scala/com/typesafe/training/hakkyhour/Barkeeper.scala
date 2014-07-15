package com.typesafe.training.hakkyhour

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props

object Barkeeper {
  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)

  def props(prepareDrinkDuration: FiniteDuration): Props =
    Props(classOf[Barkeeper], prepareDrinkDuration)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration) extends Actor with ActorLogging {
  def receive = {
    case Barkeeper.PrepareDrink(drink, guest) =>
      log.debug("making a drink...")
      busy(prepareDrinkDuration)
      sender ! Barkeeper.DrinkPrepared(drink, guest)
  }
}