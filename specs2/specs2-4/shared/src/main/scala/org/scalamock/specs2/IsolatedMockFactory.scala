package org.scalamock.specs2

import org.specs2.execute.AsResult
import org.specs2.main.ArgumentsShortcuts
import org.specs2.specification.AroundEach

/**
 * A trait that can be mixed into a [[http://etorreborre.github.com/specs2/ Specs2]] specification to provide
 * mocking support.
 *
 * $techniques
 *
 * '''Isolated tests cases''' are clean and simple, recommended when all test cases have the same
 *    or very similar fixtures
 *
 * {{{
 * class IsolatedCoffeeMachineTest extends Specification with IsolatedMockFactory {
 *
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
 * 	"CoffeeMachine" should {
 * 	    "not turn on the heater when the water container is empty" in {
 * 	        coffeeMachine.isOn must_== true
 * 	        // ...
 * 	        coffeeMachine.powerOff()
 * 	        coffeeMachine.isOn must_== false
 * 	    }
 *
 * 	    "not turn on the heater when the water container is overfull" in {
 * 	        // each test case uses separate, fresh Suite so the coffee machine is turned on
 * 	        coffeeMachine.isOn must_== true
 * 	        // ...
 * 	    }
 * 	}
 * }
 * }}}
 */
trait IsolatedMockFactory extends AroundEach with MockContextBase { self: ArgumentsShortcuts =>
  isolated

  override def around[T: AsResult](body: => T) = {
    wrapAsResult[T] { body }
  }
}
