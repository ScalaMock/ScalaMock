// Copyright (c) 2011-2015 ScalaMock Contributors (https://github.com/paulbutcher/ScalaMock/graphs/contributors)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.paulbutcher.test.matchers

import com.paulbutcher.test._
import org.scalamock.matchers.Matcher
import org.scalatest.exceptions.TestFailedException

/** Example Matcher */
case class UserMatcher(expectedName: String) extends Matcher[User] {
  override def toString = s"UserMatcher(name=$expectedName)"

  override def safeEquals(that: User): Boolean = that.name == expectedName
}

class MatchersTest extends IsolatedSpec {

  autoVerify = false

  val mockedMultiplication = mockFunction[Double, Double, Double]
  val testMock = mock[TestTrait]
  val userDatabaseMock = mock[UserDatabase]

  behavior of "MatchEpsilon"

  it should "match anything that's close to the given value" in withExpectations {
    mockedMultiplication.expects(~5.0, ~10.0)
    mockedMultiplication(5.0001, 9.9999)
  }

  it should "not match anything that's not close enough" in {
    demandExpectationException {
      mockedMultiplication.expects(~5.0, ~10.0)
      mockedMultiplication(5.1, 9.9)
    }
  }

  behavior of "MatchAny"

  it should "match anything" in withExpectations {
    (testMock.polymorphic _).expects(*).repeat(3)

    testMock.polymorphic(List("55"))
    testMock.polymorphic(List(1, 2, 3))
    testMock.polymorphic(null)
  }

  behavior of "where matcher"

  it can "be used to create complex predicates (one parameter)" in withExpectations {
    (userDatabaseMock.storeUser _).expects(where { (user: User) => user.age > 18 && user.name.startsWith("A") }).returning("matched").twice()
    (userDatabaseMock.storeUser _).expects(*).returning("unmatched").once()

    userDatabaseMock.storeUser(User("Adam", 22)) shouldBe "matched"
    userDatabaseMock.storeUser(User("Eve", 21)) shouldBe "unmatched"
    userDatabaseMock.storeUser(User("Anna", 21)) shouldBe "matched"
  }

  it can "be used to create complex predicates (two parameters)" in withExpectations {
    (testMock.twoParams _).expects(where { (x, y) => x + y > 100 }).returning("matched").twice()
    (testMock.twoParams _).expects(*, *).returning("unmatched").once()

    testMock.twoParams(99, 2.0) shouldBe "matched"
    testMock.twoParams(50, 49.0) shouldBe "unmatched"
    testMock.twoParams(50, 51.0) shouldBe "matched"
  }

  behavior of "assertArgs matcher"

  it can "be used to fail tests early (one parameter)" in withExpectations {
    (userDatabaseMock.storeUser _).expects(assertArgs { (user: User) =>
      user.age shouldBe 18
      user.name should startWith("A")
    }).returning("matched")

    (userDatabaseMock.storeUser _).expects(assertArgs { (user: User) =>
      user.age shouldBe 21
      user.name should startWith("E")
    }).returning("matched2")

    userDatabaseMock.storeUser(User("Adam", 18)) shouldBe "matched"
    userDatabaseMock.storeUser(User("Eve", 21)) shouldBe "matched2"
  }

  it can "be used to fail tests early (two parameters)" in withExpectations {
    (testMock.twoParams _).expects(assertArgs { (x, y) =>
      x + y shouldBe >(100.0)
    }).returning("matched")

    testMock.twoParams(99, 2.0) shouldBe "matched"
  }

  behavior of "argThat matcher"

  it can "be used to create complex predicates" in withExpectations {
    (userDatabaseMock.addUserAddress _)
      .expects(*, argThat { (address: Address) => address.city == "Berlin" })
      .returning("matched")
    (userDatabaseMock.addUserAddress _)
      .expects(*, argThat("Someone in London") { (address: Address) => address.city == "London" })
      .returning("matched")
    (userDatabaseMock.addUserAddress _).expects(*, *).returning("unmatched")

    userDatabaseMock.addUserAddress(User("John", 23), Address("Berlin", "Turmstrasse 12")) shouldBe "matched"
    userDatabaseMock.addUserAddress(User("John", 23), Address("Warsaw", "Marszalkowska 123")) shouldBe "unmatched"
    userDatabaseMock.addUserAddress(User("John", 23), Address("London", "Baker Street 221b")) shouldBe "matched"
  }

  it should "be displayed correctly" in withExpectations {
    val expectation = (userDatabaseMock.addUserAddress _).expects(*, argThat { (_: Address) => true }).never()
    expectation.toString() should include("UserDatabase.addUserAddress(*, argThat[Address])")
  }

  behavior of "argAssert matcher"

  it can "be used to fail tests early when assertions are not met" in withExpectations {
    val testUser = User("John", 23)

    (userDatabaseMock.addUserAddress _)
      .expects(*, argAssert { (address: Address) =>
        address.city shouldBe "Berlin" })
      .returning("matched")
    (userDatabaseMock.addUserAddress _)
      .expects(*, argAssert("Someone in London") { (address: Address) =>
        address.city shouldBe "London" })
      .returning("matched")

    userDatabaseMock.addUserAddress(testUser, Address("Berlin", "Turmstrasse 12")) shouldBe "matched"
    userDatabaseMock.addUserAddress(testUser, Address("London", "Baker Street 221b")) shouldBe "matched"
  }

  it should "fail tests immediately when assertion fails" in withExpectations {
    val testUser = User("John", 23)

    (userDatabaseMock.addUserAddress _)
      .expects(*, argAssert { (address: Address) =>
        address.city shouldBe "London" })

    a[TestFailedException] shouldBe thrownBy {
      userDatabaseMock.addUserAddress(testUser, Address("Berlin", "Turmstrasse 12"))
    }

    // call mock again with correct parameters to satisfy the expectation
    userDatabaseMock.addUserAddress(testUser, Address("London", "Baker Street 221b"))
  }

  it should "be displayed correctly" in withExpectations {
    val expectation = (userDatabaseMock.addUserAddress _).expects(*, argAssert{ (_: Address) => ()}).never()
    expectation.toString() should include("UserDatabase.addUserAddress(*, argAssert[Address])")
  }

  behavior of "custom matcher"

  it can "be used to create complex predicates" in withExpectations {
    (userDatabaseMock.addUserAddress _).expects(UserMatcher("Alan"), *).returning("matched")
    (userDatabaseMock.addUserAddress _).expects(UserMatcher("Bob"), *).returning("matched")
    (userDatabaseMock.addUserAddress _).expects(*, *).returning("unmatched")

    userDatabaseMock.addUserAddress(User("Alan", 23), Address("Berlin", "Turmstrasse 12")) shouldBe "matched"
    userDatabaseMock.addUserAddress(User("Craig", 23), Address("Warsaw", "Marszalkowska 123")) shouldBe "unmatched"
    userDatabaseMock.addUserAddress(User("Bob", 23), Address("London", "Baker Street 221b")) shouldBe "matched"
  }

  it should "be displayed correctly" in withExpectations {
    val expectation = (userDatabaseMock.addUserAddress _).expects(UserMatcher("Alan"), *).never()
    expectation.toString() should include("UserDatabase.addUserAddress(UserMatcher(name=Alan), *)")
  }

  override def newInstance = new MatchersTest
}
