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

package org.scalamock.scalatest

import org.scalamock.clazz.Mock
import org.scalatest.TestSuite

/**
 * Trait that can be mixed into a [[http://www.scalatest.org/ ScalaTest]] suite to provide
 * mocking support.
 *
 * {{{
 * class CoffeeMachineTest extends FlatSpec with ShouldMatchers with MockFactory {
 *
 * 	"CoffeeMachine" should "not turn on the heater when the water container is empty" in {
 * 	    val waterContainerMock = mock[WaterContainer]
 * 	    (waterContainerMock.isEmpty _).expects().returning(true)
 * 	    // ...
 * 	}
 *
 * 	it should "not turn on the heater when the water container is overfull" in {
 * 	    val waterContainerMock = mock[WaterContainer]
 * 	    // ...
 * 	}
 * }
 * }}}
 *
 * ==Sharing mocks across test cases==
 *
 * Sometimes multiple test cases need to work with the same mocks (and more generally - the same
 * fixtures: files, sockets, database connections, etc.). There are many techniques to avoid duplicating
 * the fixture code across test cases in ScalaTest, but ScalaMock recommends and officially supports
 * these two:
 *  - '''isolated tests cases''' - clean and simple, recommended when all test cases have the same
 *    or very similar fixtures
 *  - '''fixture contexts''' - more flexible, recommened for complex test suites where single set of
 *    fixtures does not fit all test cases
 *
 * ===Isolated test cases===
 *
 * If you mix `OneInstancePerTest` trait into a `Suite`, each test case will run in its own instance
 * of the suite class and therefore each test will get a fresh copy of the instance variables.
 *
 * This way in the suite scope you can declare instance variables (e.g. mocks) that will be used by
 * multiple test cases and perform common test case setup (e.g. set up some mock expectations).
 * Because each test case has fresh instance variables different test cases do not interfere with each
 * other.
 *
 * {{{
 * // Please note that this test suite mixes in OneInstancePerTest
 * class CoffeeMachineTest extends FlatSpec with ShouldMatchers with OneInstancePerTest with MockFactory {
 * 	// shared objects
 * 	val waterContainerMock = mock[WaterContainer]
 * 	val heaterMock = mock[Heater]
 * 	val coffeeMachine = new CoffeeMachine(waterContainerMock, heaterMock)
 *
 * 	// you can set common expectations in suite scope
 * 	(waterContainerMock.isOverfull _).expects().returning(true)
 *
 * 	// test setup
 * 	coffeeMachine.powerOn()
 *
 * 	"CoffeeMachine" should "not turn on the heater when the water container is empty" in {
 * 	    coffeeMachine.isOn shouldBe true
 * 	    // ...
 * 	    coffeeMachine.powerOff()
 * 	}
 *
 * 	it should "not turn on the heater when the water container is overfull" in {
 * 	    // each test case uses separate, fresh Suite so the coffee machine is turned on
 * 	    coffeeMachine.isOn shouldBe true
 * 	    // ...
 * 	}
 * }
 * }}}
 *
 * ===Fixture contexts===
 *
 * You can also run each test case in separate fixture context. Fixture contexts can be extended
 * and combined and since each test case uses different instance of fixture context test cases do not
 * interfere with each other while they can have shared mocks and expectations.
 *
 * {{{
 * class CoffeeMachineTest extends FlatSpec with ShouldMatchers with MockFactory {
 * 	trait Test { // fixture context
 * 	    // shared objects
 * 	    val waterContainerMock = mock[WaterContainer]
 * 	    val heaterMock = mock[Heater]
 * 	    val coffeeMachine = new CoffeeMachine(waterContainerMock, heaterMock)
 *
 * 	    // test setup
 * 	    coffeeMachine.powerOn()
 * 	}
 *
 * 	"CoffeeMachine" should "not turn on the heater when the water container is empty" in new Test {
 * 	    coffeeMachine.isOn shouldBe true
 * 	    (waterContainerMock.isOverfull _).expects().returning(true)
 * 	    // ...
 * 	}
 *
 * 	// you can extend and combine fixture-contexts
 * 	trait OverfullWaterContainerTest extends Test {
 * 	    // you can set expectations and use mocks in fixture-context
 * 	    (waterContainerMock.isEmpty _).expects().returning(true)
 *
 * 	    // and define helper functions
 * 	    def complexLogic() {
 * 	      coffeeMachine.powerOff()
 * 	      // ...
 * 	    }
 * 	}
 *
 * 	it should "not turn on the heater when the water container is overfull" in new OverfullWaterContainerTest {
 * 	    // ...
 * 	    complexLogic()
 * 	}
 * }
 * }}}
 *
 * See [[org.scalamock]] for overview documentation.
 */
trait MockFactory extends AbstractMockFactory with Mock { this: TestSuite =>

}
