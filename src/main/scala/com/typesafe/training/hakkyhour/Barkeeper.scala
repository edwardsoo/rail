package com.typesafe.training.hakkyhour

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import scala.util.Random
import akka.actor.Stash
import akka.actor.FSM

object Barkeeper {
  sealed abstract class State
  case object Ready extends State
  case object Busy extends State

  sealed abstract class Data
  case object NoOrder extends Data
  case class Order(drink: Drink, guest: ActorRef, waiter: ActorRef) extends Data

  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)

  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int): Props =
    Props(classOf[Barkeeper], prepareDrinkDuration, accuracy)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int)
    extends Actor with FSM[Barkeeper.State, Barkeeper.Data] with ActorLogging with Stash {
  import Barkeeper._

  val rand = new Random
  startWith(Ready, NoOrder)

  when(Ready) {
    case Event(PrepareDrink(drink, guest), NoOrder) =>
      // log.debug("making a drink...")
      goto(Busy) using (Order(drink, guest, sender))
  }

  when(Busy, prepareDrinkDuration) {
    case Event(StateTimeout, Order(drink, guest, waiter)) =>
      if (rand.nextInt(100) < accuracy) {
        waiter ! Barkeeper.DrinkPrepared(drink, guest)
      } else {
        waiter ! Barkeeper.DrinkPrepared(Drink.anyOther(drink), guest)
      }
      unstashAll()
      goto(Ready) using NoOrder
    case _ =>
      stash()
      stay()
  }

  initialize()
}