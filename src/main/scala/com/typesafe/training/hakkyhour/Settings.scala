package com.typesafe.training.hakkyhour

import akka.actor.ExtensionKey
import akka.actor.Extension
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.FiniteDuration
import akka.actor.Actor
import akka.actor.ExtendedActorSystem

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {
  val finishDrinkDuration =
    FiniteDuration(
      system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", SECONDS),
      SECONDS)
  val barkeeperPrepareDrinkDuration =
    FiniteDuration(
      system.settings.config.getDuration("hakky-hour.barkeeper.prepare-drink-duration", SECONDS),
      SECONDS)
  val maxComplaint =
    system.settings.config.getInt("hakky-hour.waiter.max-complaint-count")
  val barkeeperAccuracy =
    system.settings.config.getInt("hakky-hour.barkeeper.accuracy")
  val statusTimeout =
    FiniteDuration(
      system.settings.config.getDuration("hakky-hour.status-timeout", SECONDS),
      SECONDS)
  val maxDrinkCount = system.settings.config.getInt("hakky-hour.max-drink-count")
}

trait SettingsActor {
  this: Actor =>
  val settings: Settings = Settings(context.system)
}
