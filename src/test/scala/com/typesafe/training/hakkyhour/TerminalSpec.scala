/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import org.scalatest.{ Matchers, Inspectors, WordSpec }

class TerminalSpec extends WordSpec with Matchers with Inspectors with Terminal {

  "Calling Command.apply" should {
    "create the correct CreateGuest command for the given input" in {
      Command("guest") shouldEqual Command.CreateGuest(1, Drink.Akkarita, false, Int.MaxValue)
      Command("2 g") shouldEqual Command.CreateGuest(2, Drink.Akkarita, false, Int.MaxValue)
      Command("g m") shouldEqual Command.CreateGuest(1, Drink.MaiPlay, false, Int.MaxValue)
      Command("g s") shouldEqual Command.CreateGuest(1, Drink.Akkarita, true, Int.MaxValue)
      Command("g 1") shouldEqual Command.CreateGuest(1, Drink.Akkarita, false, 1)
      Command("2 g m s 1") shouldEqual Command.CreateGuest(2, Drink.MaiPlay, true, 1)
    }
    "create the GetStatus command for the given input" in {
      Command("status") shouldEqual Command.GetStatus
      Command("s") shouldEqual Command.GetStatus
    }
    "create the Quit command for the given input" in {
      Command("quit") shouldEqual Command.Quit
      Command("q") shouldEqual Command.Quit
    }
    "create the Unknown command for illegal input" in {
      Command("foo") shouldEqual Command.Unknown("foo")
    }
  }
}
