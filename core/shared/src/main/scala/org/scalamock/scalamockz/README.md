# scalamockz

A library for working with ScalaMock in ZIO Test.

Table of Contents:
- [Quick Start](#quick-start)
- [Setting up mock expectations](#setting-up-mock-expectations)
- [Stubs in Record-then-Verify style](#stubs-in-record-then-verify-style)
- [Injecting mocks and stubs into ZLayer](#injecting-mocks-and-stubs-into-zlayer)
- [Effect call verification](#effect-call-verification)
- [Call order verification](#call-order-verification)

## Quick Start

```scala
import org.scalamock.scalamockz._
import zio.test._
import zio._

// ScalamockZSpec — a trait with everything needed for working with ScalaMock in ZIO Test
object ApiServiceSpec extends ScalamockZSpec {

  override def spec: MockedSpec =
    suite("ApiServiceSpec")(
      test("return greeting")(
        for {
          // Setting up expectations — how the mock should be called and what it should return
          _ <- ZIO.serviceWith[UserService] { mock =>
            (mock.getUserName _).expects(4).returnsZIO("Agent Smith")
          }
          // Calling the tested code
          result <- ZIO.serviceWithZIO[ApiService](_.getGreeting(4))
        } yield assertTrue(result == "Hello, Agent Smith!")
      )
    ).provideSome(ApiService.layer, mock[UserService]) // Providing the required mock in the test
}
```

## Setting up mock expectations

To set up mock expectations, you need to obtain the mock from the ZIO Environment and then call the ScalaMock DSL:

```scala
ZIO.serviceWith[UserService] { mock =>
  (mock.getName _).expects(4).returnsZIO("Agent Smith")
}
```

You can use DSL methods to set up expectations just like in ScalaMock for ScalaTest.

Examples of using this DSL can be found in [ScalaMock's feature documentation](https://scalamock.org/user-guide/features/), under ScalaMock (not ScalaMock 7!).

Additionally, there are helpers for mocking ZIO methods:

```scala
// shortcut for returns(ZIO.succeed("Agent Smith")
(mock.getName _).expects(4).returnsZIO("Agent Smith")

// shortcut for returns(ZIO.fail(new Exception("test"))
(mock.getName _).expects(4).failsZIO(new Exception("test"))

// shortcut for returns(ZIO.die(new RuntimeException("death"))
(mock.getName _).expects(4).diesZIO(new RuntimeException("death"))
```

From the [above-mentioned documentation](https://scalamock.org/user-guide/features/), all features work in scalamockz except Ordering.

Thus, the following features are supported:
- Argument Matching
- Call counts
- Returning values
- Throwing exceptions (however, when mocking ZIO methods, it is better to use `failsZIO()` or `diesZIO()`)
- Call handlers

Ordering is also supported, but with some limitations.
If you need to specify the call order of mocks, refer to the section [Call order verification](#call-order-verification) below.

## Stubs in Record-then-Verify style

https://scalamock.org/user-guide/mocking_style/

In ScalaMock, there are not only mocks but also stubs. These are also supported in scalamockz:

```scala
object ApiServiceSpec extends ScalamockZSpec {

  override def spec: MockedSpec =
    suite("ApiServiceSpec")(
      test("return greeting")(
        for {
          // Setting up expectations — how the stub may (but does not have to) be called, and what it should return.
          _ <- ZIO.serviceWith[UserService] { stub =>
            (stub.getUserName _).when(*).returnsZIO("Agent Smith")
          }
          // Calling the tested code
          result <- ZIO.serviceWithZIO[ApiService](_.getGreeting(4))
          // Verifying that stub was called as expected
          _ <- ZIO.serviceWith[UserService] { stub =>
            (stub.getUserName _).verify(4)
          }
        } yield assertTrue(result == "Hello, Agent Smith!")
      )
    ).provideSome(ApiService.layer, stub[UserService]) // Providing the required stub in the test
}
```

This allows writing tests in the arrange-act-assert style, which in some cases leads to more readable code.

## Injecting mocks and stubs into ZLayer

When setting up expectations, we require a mock of the appropriate type to be available in the ZIO Environment.
Now we need to inject this mock into the ZIO Environment.

For this, you should use the `mock[A]` or `stub[A]` method, which returns a `URLayer[ScalamockZFactory, A]`.
This is a recipe for creating a mock or stub that needs to be injected into the tested code through `ZLayer`:

```scala
override def spec: MockedSpec =
  suite("ApiServiceSpec")(
    test("return greeting")(
      // Test code mocking UserService and AuthService...
    )
  ).provideSome(ApiService.layer, mock[UserService], stub[AuthService])
```

Note: We cannot use `provide()` here because `ScalamockZFactory` is not provided in this case.
It is provided in `ScalamockZSpec`, and the library user does not need to create it.
It is only necessary to specify (via `provideSome`) that it will be created outside of the test, later.

`ScalamockZFactory` is an object that creates mocks from the `mock[A]` recipe and verifies expectations after each test.

It is created separately for each test, ensuring that mocks are independent across tests.

**Warning:** Injecting mocks through `provideSomeShared()` will cause the test to fail with an internal ScalaMock error:

```
assertion failed: Null expectation context - missing withExpectations?
```

This happens because expectations in scalamockz must be set up for each test separately.
Thus, mocks should be created separately for each test, not shared between tests.

## Effect call verification

ScalaMock for ScalaTest can verify that a method was called.
However, if we mock a method that returns an effect, we cannot check that the effect was actually called.

To avoid this, in scalamockz, mocks from ScalaMock are additionally wrapped to verify that the effect was called.

For example, here’s a service with a bug:

```scala
trait UserService {
  def getUserName(id: Int): UIO[String]
  def setGreeting(greeting: String): UIO[Unit]
}

class ApiService(userService: UserService) {

  def setGreeting(userId: Int): UIO[Unit] =
    for {
      userName <- userService.getUserName(userId)
      greeting = s"Hello, $userName!"
      _ = userService.setGreeting(greeting) // Effect is not invoked in this case!
    } yield ()
}
```

Here’s a test for it:

```scala
test("set greeting for user")(
  for {
    _ <- ZIO.serviceWith[UserService] { mock =>
      (mock.getUserName _).expects(4).returnsZIO("Agent Smith")
      (mock.setGreeting _).expects("Hello, Agent Smith!").returnsZIO(())
    }
    _ <- ZIO.serviceWithZIO[ApiService](_.setGreeting(4))
  } yield assertCompletes
)
```

This test fails:

```
Unsatisfied expectation:
 
Expected:
inAnyOrder {
  <mock-1> UserService.getUserName(4) once (called once)
  <mock-1> UserService.setGreeting(Hello, Agent Smith!) once (never called - UNSATISFIED)
}

Actual:
  <mock-1> UserService.getUserName(4)
```

This allows us to catch bugs caused by forgotten effect calls, unlike ScalaMock for ScalaTest.

In scalamockz, this works by default for all methods returning `ZIO` (including `Task`, `URIO`, etc.).
No additional configuration is needed for this.

Additionally, methods returning non-`ZIO` can also be mocked.
This mocking works just like in ScalaMock for ScalaTest — without verifying the effect call.

## Call order verification

The order of mocked method calls can be verified.
In ScalaMock for ScalaTest, the methods `inSequence` and `inAnyOrder` are used for this.
If neither of these methods is called, ScalaMock does not check the call order by default.

In scalamockz, `inSequence` returns an effect.
For example, this test verifies both that the methods are called and that `isAuthorized` is called before `getUserName`.

```scala
test("return greeting if user is authorized")(
  for {
    authService <- ZIO.service[AuthService]
    userService <- ZIO.service[UserService]
    _ <- inSequence {
      (authService.isAuthorized _).expects(4).returnsZIO(true)
      (userService.getUserName _).expects(4).returnsZIO("Agent Smith")
    }
    result <- ZIO.serviceWithZIO[ApiService](_.getGreeting(4))
  } yield assertTrue(result == "Hello, Agent Smith!")
)
```

If `inSequence` is not called, the call order verification will not occur. There is no explicit `inAnyOrder` method in scalamockz.
