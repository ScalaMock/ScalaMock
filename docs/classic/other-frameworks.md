---
layout: default
title: Other Frameworks
parent: Classic
nav_order: 3
permalink: /classic/other-frameworks/
---

# Using ScalaMock without ScalaTest, Specs2 or ZIO Test

You don't need ScalaTest, Specs2 or ZIO Test to use **scalamock**. All the integrations above are just thin
adapters around `org.scalamock.MockFactoryBase`, so you can implement your own subtype of it and adapt
**scalamock** to any other testing framework (JUnit, MUnit, uTest, etc.), or use it without a framework at all.

To do that you need to:

1. Extend `org.scalamock.MockFactoryBase` (this also brings `mock[T]`/`stub[T]` into scope).
2. Provide the `ExpectationException` type member and `newExpectationException` - the exception thrown when
   an expectation is not met.
3. Wrap each test case body with `withExpectations { ... }` - it resets call logs/expectations before the
   test case and verifies them once the body completes.

```scala
//> using dep org.scalamock::scalamock:7.5.0

import org.scalamock.MockFactoryBase

trait Cat {
  def meow(): Unit
  def isHungry: Boolean
}

class MyMockFactory extends MockFactoryBase {
  type ExpectationException = Exception

  override protected def newExpectationException(message: String, methodName: Option[Symbol]): Exception =
    new Exception(s"$message, $methodName")

  // expose withExpectations under a more descriptive name for test cases to use
  def test[T](body: => T): T = withExpectations(body)
}

@main def run(): Unit =
  given mc: MyMockFactory = new MyMockFactory

  mc.test {
    // and am mocking a cat
    val cat = mc.mock[Cat]
    // and the cat meows
    cat.meow.expects().returns(())
    // and the cat is always hungry
    cat.isHungry.expects().returns(true).anyNumberOfTimes()

    // then the cat needs feeding
    assert(cat.isHungry)

    cat.meow()
    // withExpectations verifies here that cat.meow() was called exactly once
  }
```

{: .note }
> Each test case should use its own `MyMockFactory` instance (or run inside its own `withExpectations` block),
> otherwise mocks and expectations may leak between test cases, similarly to sharing mocks between ScalaTest
> or Specs2 test cases.

If your framework runs test cases as ordinary methods/functions, a common pattern is to create a fresh
`MockFactoryBase` instance per test case, e.g. in a `setUp`/fixture method, and call `withExpectations` around
the test body, the same way `org.scalamock.scalatest.MockFactory` wraps ScalaTest's `withFixture` or
`org.scalamock.specs2.MockContext` wraps a Specs2 fixture context.
