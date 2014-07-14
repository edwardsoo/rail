package com.typesafe.training.hakkyhour

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.ActorRef
import scala.concurrent.duration.FiniteDuration

class Guest(
    waiter: ActorRef,
    favoriteDrink: Drink,
    finishDrinkDuration: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount = 0

  override def preStart = {
    waiter ! Waiter.ServeDrink(favoriteDrink)
  }

  override def receive = {
    case Waiter.DrinkServed(drink) => {
      drinkCount += 1
      log.info(s"Enjoying my ${drinkCount}. yummy ${drink}!")
      context.system.scheduler.scheduleOnce(finishDrinkDuration, self, Guest.DrinkFinished)
    }
    case Guest.DrinkFinished => waiter ! Waiter.ServeDrink(favoriteDrink)
  }
}

object Guest {
  private object DrinkFinished
  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration): Props =
    Props(classOf[Guest], waiter, favoriteDrink, finishDrinkDuration)
}