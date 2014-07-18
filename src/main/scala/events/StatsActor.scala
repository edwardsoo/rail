package events

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import scala.collection.mutable.Map
import java.util.Date
import java.util.Calendar
import scala.collection.mutable.Buffer

object Stats {
  def props = Props(classOf[Stats])
}

class Stats extends Actor with ActorLogging {

  val browserCount: Map[String, Int] = Map.empty withDefaultValue 0
  val referrerCount: Map[String, Int] = Map.empty withDefaultValue 0
  val minuteCount: Array[Int] = Array.fill(60 * 24)(0)
  val landingCount: Map[String, Int] = Map.empty withDefaultValue 0
  val sinkingCount: Map[String, Int] = Map.empty withDefaultValue 0
  val visitTimes: Buffer[Int] = Buffer.empty
  val pageViews: Map[String, Seq[Int]] = Map.empty withDefaultValue Seq()

  val jsonMap = Map("browserCount" -> browserCount,
    "referrerCount" -> referrerCount,
    "minuteCount" -> minuteCount,
    "landingCount" -> landingCount,
    "sinkingCount" -> sinkingCount,
    "visitTimes" -> visitTimes,
    "pageViews" -> pageViews)

  def busiestMinuteOfDay: Tuple2[Int, Int] =
    minuteCount.zipWithIndex.foldLeft[Tuple2[Int, Int]]((0, 0)) {
      case ((busiest, max), (count, index)) =>
        if (count > max) (index, count) else (busiest, max)
    }

  def averagePageViews: Map[String, Double] =
    pageViews.map { case (page, views) => (page -> (views.reduce(_ + _).toDouble / views.size)) }

  def averageVisitTime: Double =
    visitTimes.reduce(_ + _).toDouble / visitTimes.size

  def top5[T](counter: Map[T, Int]): List[Tuple2[T, Int]] =
    counter.toList.sortBy { case (_, count) => count }.reverse.take(5)

  def top5Landing: List[Tuple2[String, Int]] = top5(landingCount)
  def top5Sinking: List[Tuple2[String, Int]] = top5(sinkingCount)
  def top5browser: List[Tuple2[String, Int]] = top5(browserCount)
  def top5Referrer: List[Tuple2[String, Int]] = top5(referrerCount)

  // Transform UNIX time in ms to minute of day
  def minuteOfDay(timestamp: Long): Int =
    ((timestamp % (60 * 60 * 24 * 1000)) / 60 * 1000).toInt

  def receive = {
    case UserTracker.History(history) =>
      log.info("got history")

      val session = history.head.session
      browserCount(session.browser) += 1
      referrerCount(session.referrer) += 1
      history.map(req => minuteCount(minuteOfDay(req.timestamp)) += 1)
      landingCount(history.head.url) += 1
      sinkingCount(history.last.url) += 1
      visitTimes += session.duration
      history.map(req => req.url).groupBy(url => url).map {
        case (url, ls) => (url, ls.size)
      }.foreach {
        case (url, view) => pageViews(url) = pageViews(url) :+ view
      }
  }
}