package com.typesafe.training.hakkyhour

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging

class Waiter extends Actor with ActorLogging {
  def receive = {
    case Waiter.ServeDrink(drink) => sender ! Waiter.DrinkServed(drink)
  }
}

object Waiter {
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)
  def props = Props[Waiter]
}