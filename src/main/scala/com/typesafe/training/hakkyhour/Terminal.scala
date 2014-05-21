/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import scala.util.parsing.combinator.RegexParsers

trait Terminal {

  sealed trait Command

  object Command {

    case class CreateGuest(count: Int, drink: Drink, isStubborn: Boolean, maxDrinkCount: Int) extends Command

    case object GetStatus extends Command

    case object Quit extends Command

    case class Unknown(command: String) extends Command

    def apply(command: String): Command =
      CommandParser.parseAsCommand(command)
  }

  object CommandParser extends RegexParsers {

    def parseAsCommand(s: String): Command =
      parseAll(parser, s) match {
        case Success(command, _) => command
        case _                   => Command.Unknown(s)
      }

    def createGuest: Parser[Command.CreateGuest] =
      opt(int) ~ ("guest|g".r ~> opt(drink) ~ opt("s".r) ~ opt(int)) ^^ {
        case count ~ (drink ~ stubborn ~ maxDrinkCount) =>
          Command.CreateGuest(
            count getOrElse 1,
            drink getOrElse Drink.Akkarita,
            stubborn.isDefined,
            maxDrinkCount getOrElse Int.MaxValue
          )
      }

    def getStatus: Parser[Command.GetStatus.type] =
      "status|s".r ^^ (_ => Command.GetStatus)

    def quit: Parser[Command.Quit.type] =
      "quit|q".r ^^ (_ => Command.Quit)

    def drink: Parser[Drink] =
      "A|a|M|m|P|p".r ^^ Drink.apply

    def int: Parser[Int] =
      """\d+""".r ^^ (_.toInt)
  }

  val parser: CommandParser.Parser[Command] =
    CommandParser.createGuest | CommandParser.getStatus | CommandParser.quit
}
