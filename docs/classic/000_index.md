---
layout: default
title: Classic
nav_order: 4
permalink: /classic/
has_children: true
---

# Classic

Classic scalamock is the original project that has been available since the beginning. 
It provides a powerful and flexible way to create and use mocks and stubs in your tests.

It offers:
- **Two mocking styles**: Expectations-First Style (mocks) and Record-then-Verify (stubs)
- **Scalatest, Spec2 and ZIO Test integration**
- **Powerful features**: Advanced expectations, call ordering, argument matchers and more


## Getting Started

The first rule of **scalamock** is not to share any mocks and stubs between your test cases.

Usually - you should create some fixture/wiring to be reused in each test-case.

### Scalatest

To use **scalamock** with **scalatest** - your suite should mixin `org.scalamock.scalatest.MockFactory`

```scala
//> using test.dep org.scalamock::scalamock:7.4.1
//> using test.dep org.scalatest::scalatest:3.2.19

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class MyTest extends AnyFlatSpec, MockFactory:
  trait Wiring:
    val service1 = stub[Service1]
    val service2 = stub[Service2]
    val service3 = Service3(service1, service2)

  it should "do something" in new Wiring {
    // your test logic here
  }

```

When testing with futures - you have two options:

1. Mixin `org.scalatest.concurrent.ScalaFutures` and override patience configuration
2. Or use async suites like `AsyncFlatSpec` and mixin `org.scalamock.scalatest.AsyncMockFactory`

```scala

class ExchangeRateListingTest extends AsyncFlatSpec with AsyncMockFactory {

  val eur = Currency(id = "EUR", valueToUSD = 1.0531, change = -0.0016)
  val gpb = Currency(id = "GPB", valueToUSD = 1.2280, change = -0.0012)
  val aud = Currency(id = "AUD", valueToUSD = 0.7656, change = -0.0024)

  "ExchangeRateListing" should "eventually return the exchange rate between passed Currencies when getExchangeRate is invoked" in {
      val currencyDatabaseStub = stub[CurrencyDatabase]
      currencyDatabaseStub.getCurrency.when(eur.id).returns(eur)
      currencyDatabaseStub.getCurrency.when(gpb.id).returns(gpb)
      currencyDatabaseStub.getCurrency.when(aud.id).returns(aud)
      
      val listing = new ExchangeRateListing(currencyDatabaseStub)
      
      val future: Future[Double] = listing.getExchangeRate(eur.id, gpb.id)
      
      future.map(exchangeRate => assert(exchangeRate == eur.valueToUSD / gpb.valueToUSD))
  }
}
```
### Specs2
To use **scalamock** with **specs2** you should run each test case in a separate fixture context that mixins `org.scalamock.specs2.MockContext`

```scala
//> using test.dep org.scalamock::scalamock:7.4.1
//> using test.dep org.specs2::specs2-core:5.6.3

import org.scalamock.specs2.MockContext
import org.specs2.mutable.Specification

class MySpec extends Specification {

  trait Wiring extends MockContext {
    val service1 = stub[Service1]
    val service2 = stub[Service2]
    val service3 = Service3(service1, service2)
  }
  
  "CoffeeMachine" should {
    "not turn on the heater when the water container is empty" in new Wiring {
      val waterContainerMock = mock[WaterContainer]
      waterContainerMock.isEmpty.expects().returning(true)
    }
  }
}
```

### ZIO Test

To use **scalamock** with **ZIO Test**, you should extend `org.scalamock.ziotest.ScalamockZIOSpec`. This integration provides a classic mocking style with ZIO-specific enhancements.

For detailed ZIO Test integration guide, see [ZIO Test Integration](/classic/zio-test/).

```scala
//> using dep dev.zio::zio:2.1.19
//> using test.dep org.scalamock::scalamock-zio:7.5.0
//> using test.dep dev.zio::zio-test:2.1.19

import org.scalamock.ziotest._
import zio._
import zio.test._

trait UserService {
  def getUserName(id: Int): UIO[String]
}

class ApiService(userService: UserService) {
  def getGreeting(id: Int): UIO[String] =
    userService.getUserName(id).map(name => s"Hello, $name!")
}

object ApiService {
  val layer = ZLayer.derive[ApiService]
}

object ApiServiceSpec extends ScalamockZIOSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("ApiServiceSpec")(
      test("return greeting")(
        for {
          // Setup expectations - how mock should be called and what it returns
          _ <- ZIO.serviceWith[UserService] { mock =>
            (mock.getUserName _).expects(4).returnsZIO("Agent Smith")
          }
          // Call code under test
          result <- ZIO.serviceWithZIO[ApiService](_.getGreeting(4))
        } yield assertTrue(result == "Hello, Agent Smith!")
      )
    ).provide(ApiService.layer, mock[UserService]) // Provide required mock for the test
}

```

### Other frameworks

Not using ScalaTest, Specs2 or ZIO Test? You can still use **scalamock** by implementing your own subtype of `org.scalamock.MockFactoryBase`. This makes it possible to adapt ScalaMock to any testing framework (JUnit, MUnit, uTest, etc.), or use it without a framework at all.

For a detailed guide, see [Other Frameworks](/classic/other-frameworks/).