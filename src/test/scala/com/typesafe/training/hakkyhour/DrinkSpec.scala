/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import org.scalatest.{ Matchers, Inspectors, WordSpec }

class DrinkSpec extends WordSpec with Matchers with Inspectors {

  import Drink._

  "drinks" should {
    "contain Akkarita, MaiPlay and PinaScalada" in {
      drinks shouldEqual Set(Akkarita, MaiPlay, PinaScalada)
    }
  }

  "Calling apply" should {
    "create the correct Drink for a known code" in {
      apply("A") shouldEqual Akkarita
      apply("a") shouldEqual Akkarita
      apply("M") shouldEqual MaiPlay
      apply("m") shouldEqual MaiPlay
      apply("P") shouldEqual PinaScalada
      apply("p") shouldEqual PinaScalada
    }
    "throw an IllegalArgumentException for an unknown code" in {
      an[IllegalArgumentException] should be thrownBy apply("1")
    }
  }

  "Calling anyOther" should {
    "return an other Drink than the given one" in {
      forAll(drinks) { drink => anyOther(drink) should not equal drink }
    }
  }
}
