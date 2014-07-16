package com.typesafe.training.hakkyhour

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.actor.ActorRef

class Waiter(hakkyHour: ActorRef, barkeeper: ActorRef, maxComplaintCount: Int) extends Actor with ActorLogging {
  log.info(s"boss is ${hakkyHour}")
  var nrComplaint = 0

  def receive = {
    case Waiter.ServeDrink(drink) =>
      log.debug("drink needs approval")
      hakkyHour ! HakkyHour.ApproveDrink(drink, sender)
    case Barkeeper.DrinkPrepared(drink, guest) =>
      log.debug("got the drink")
      guest ! Waiter.DrinkServed(drink)
    case Waiter.Complaint(drink) =>
      nrComplaint += 1
      log.debug("got complaint...")
      if (nrComplaint > maxComplaintCount) {
        log.debug("I quit")
        throw Waiter.FrustratedException(drink, sender)
      }
      barkeeper ! Barkeeper.PrepareDrink(drink, sender)

  }
}

object Waiter {
  case class FrustratedException(drink: Drink, victim: ActorRef) extends Exception
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)
  case class Complaint(drink: Drink)
  def props(hakkyHour: ActorRef, barkeeper: ActorRef, maxComplaintCount: Int) =
    Props(classOf[Waiter], hakkyHour, barkeeper, maxComplaintCount)
}