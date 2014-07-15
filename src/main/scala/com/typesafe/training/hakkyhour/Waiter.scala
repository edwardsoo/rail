package com.typesafe.training.hakkyhour

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.actor.ActorRef

class Waiter(hakkyHour: ActorRef) extends Actor with ActorLogging {
  log.info(s"boss is ${hakkyHour}")

  def receive = {
    case Waiter.ServeDrink(drink) =>
      log.debug("drink needs approval")
      hakkyHour ! HakkyHour.ApproveDrink(drink, sender)
    case Barkeeper.DrinkPrepared(drink, guest) =>
      log.debug("got the drink")
      guest ! Waiter.DrinkServed(drink)
  }
}

object Waiter {
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)
  def props(hakkyHour: ActorRef) = Props(classOf[Waiter], hakkyHour)
}