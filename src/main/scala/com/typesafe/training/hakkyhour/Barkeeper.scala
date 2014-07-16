package com.typesafe.training.hakkyhour

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import scala.util.Random

object Barkeeper {
  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)

  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int): Props =
    Props(classOf[Barkeeper], prepareDrinkDuration, accuracy)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor with ActorLogging {
  val rand = new Random
  def receive = {
    case Barkeeper.PrepareDrink(drink, guest) =>
      log.debug("making a drink...")
      busy(prepareDrinkDuration)
      if (rand.nextInt(100) < accuracy)
        sender ! Barkeeper.DrinkPrepared(drink, guest)
      else
        sender ! Barkeeper.DrinkPrepared(Drink.anyOther(drink), guest)
  }
}