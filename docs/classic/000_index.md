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

{: .note }
> As of **7.6.0**, test framework integrations are being extracted out of the core `scalamock` artifact into
> their own published modules with their own dependency on the corresponding framework - see the
> [Specs2](#specs2) section below. **Scalatest** integration is still bundled in the core `scalamock` module
> for now, but will be extracted into its own `scalamock-scalatest` module the same way in an upcoming release.

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

Specs2 integration is split into two modules, depending on which major version of specs2 you use:

- `scalamock-specs2-4` - for specs2 4.x, available for Scala 2.13 and Scala 3
- `scalamock-specs2-5` - for specs2 5.x, available for Scala 3 only (specs2 5.x dropped Scala 2 support entirely)

Both modules provide the same `org.scalamock.specs2.MockContext` fixture-context trait. To use **scalamock** with **specs2** you should run each test case in a separate fixture context that mixins `org.scalamock.specs2.MockContext`

```scala
//> using test.dep org.scalamock::scalamock-specs2-4:7.6.0
//> using test.dep org.specs2::specs2-core:4.23.0

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

To use it with specs2 5.x instead, swap the dependency for `scalamock-specs2-5` (Scala 3 only):

```scala
//> using test.dep org.scalamock::scalamock-specs2-5:7.6.0
//> using test.dep org.specs2::specs2-core:5.9.1
```

{: .note }
> Specs2 5.x removed "isolated" specifications, so a single specification instance (and any mocks defined
> in its suite scope, outside a fixture context) is now shared across all of its examples. If you need
> **suite-scope** mocks with specs2 5.x, mixin `org.scalamock.specs2.SuiteMockFactory` instead of/alongside
> `MockContext` - it serializes example execution for that specification via a lock so suite-scope mocks
> stay safe even when specs2 schedules examples of different specifications in parallel.

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