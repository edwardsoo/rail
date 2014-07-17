package events

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import scala.collection.mutable.Map
import java.util.Date
import java.util.Calendar

object Stats {
  def props = Props(classOf[Stats])
}

class Stats extends Actor with ActorLogging {

  val browserCount: Map[String, Int] = Map.empty withDefaultValue 0
  val referrerCount: Map[String, Int] = Map.empty withDefaultValue 0
  val minuteCount: Map[Int, Int] = Map.empty withDefaultValue 0
  val landingCount: Map[String, Int] = Map.empty withDefaultValue 0
  val sinkingCount: Map[String, Int] = Map.empty withDefaultValue 0
  var visitTimes: Seq[Int] = Seq.empty
  val pageViews: Map[String, Seq[Int]] = Map.empty withDefaultValue Seq()

  def busiestMinuteOfDay: Option[Tuple2[Int, Int]] =
    minuteCount.foldLeft[Option[Tuple2[Int, Int]]](None) {
      case (Some(acc), elem) =>
        val (busiest, max) = acc
        val (minute, count) = elem
        if (count > max) Some(elem) else Some(acc)
      case (None, elem) => Some(elem)
    }

  def averagePageViews: Map[String, Double] =
    pageViews.map { case (page, views) => (page -> (views.reduce(_ + _).toDouble / views.size)) }

  def averageVisitTime: Double =
    visitTimes.reduce(_ + _).toDouble / visitTimes.size

  def top5[T](counter: Map[T, Int]): Seq[Tuple2[T, Int]] =
    counter.toList.sortBy { case (_, count) => count }.reverse.take(5)

  def top5Landing: Seq[Tuple2[String, Int]] = top5(landingCount)
  def top5Sinking: Seq[Tuple2[String, Int]] = top5(sinkingCount)
  def top5browser: Seq[Tuple2[String, Int]] = top5(browserCount)
  def top5Referrer: Seq[Tuple2[String, Int]] = top5(referrerCount)

  // Transform UNIX time in ms to minute of day
  def minuteOfDay(timestamp: Long): Int =
    ((timestamp % (60*60*24*1000)) / 60*1000).toInt
  
  def receive = {
    case UserTracker.History(history) =>
      log.info("got history")

      val session = history.head.session
      browserCount(session.browser) += 1
      referrerCount(session.referrer) += 1
      history.map(req => minuteCount(minuteOfDay(req.timestamp)) += 1)
      landingCount(history.head.url) += 1
      sinkingCount(history.last.url) += 1
      visitTimes = visitTimes :+ session.duration
      history.map(req => req.url).groupBy(url => url).map {
        case (url, ls) => (url, ls.size)
      }.foreach {
        case (url, view) => pageViews(url) = pageViews(url) :+ view
      }
  }
}