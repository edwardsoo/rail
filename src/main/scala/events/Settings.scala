package event

import akka.actor.ExtensionKey
import akka.actor.Extension
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.FiniteDuration
import akka.actor.Actor
import akka.actor.ExtendedActorSystem

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {
  val timerInterval = FiniteDuration(system.settings.config.getDuration("session-manager.user-tracker.timer-interval", SECONDS), SECONDS)
  //  val shortVisit = FiniteDuration(system.settings.config.getDuration("session-manager.event-stream.short-visit", SECONDS), SECONDS)
  //  val longVisit = FiniteDuration(system.settings.config.getDuration("session-manager.event-stream.long-visit", SECONDS), SECONDS)
}

trait SettingsActor {
  this: Actor =>
  val settings: Settings = Settings(context.system)
}
