package com.typesafe.training.hakkyhour

import scala.concurrent.duration.FiniteDuration

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object Guest {
  private object DrinkFinished
  object DrunkException extends Exception
  def props(waiter: ActorRef,
    favoriteDrink: Drink,
    finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean,
    maxDrinkCount: Int): Props =
    Props(classOf[Guest], waiter, favoriteDrink, finishDrinkDuration, isStubborn, maxDrinkCount)
}

class Guest(
    waiter: ActorRef,
    favoriteDrink: Drink,
    finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean,
    maxDrinkCount: Int) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount = 0

  override def preStart = {
    // log.info("hey give me a drink")
    waiter ! Waiter.ServeDrink(favoriteDrink)
  }

  override def postStop = {
    log.info("Good-bye!")
  }

  override def receive = {
    case HakkyHour.NoMoreDrinks =>
      if (isStubborn)
        waiter ! Waiter.ServeDrink(favoriteDrink)
      else {
        log.info("All right, time to go home!")
        context.stop(self)
      }
    case Waiter.DrinkServed(drink) if drink == favoriteDrink => {
      drinkCount += 1
      log.info(s"Enjoying my ${drinkCount}. yummy ${drink}!")
      context.system.scheduler.scheduleOnce(finishDrinkDuration, self, Guest.DrinkFinished)
    }
    case Waiter.DrinkServed(drink) => {
      // log.debug("that's not what I ordered")
      waiter ! Waiter.Complaint(favoriteDrink)
    }
    case Guest.DrinkFinished => {
      if (drinkCount > maxDrinkCount) throw Guest.DrunkException
      waiter ! Waiter.ServeDrink(favoriteDrink)
    }
  }
}