package org.scalamock.specs2

import org.specs2.execute.AsResult
import org.specs2.specification.AroundEach

/**
 * A trait that can be mixed into a [[http://etorreborre.github.com/specs2/ Specs2]] specification to provide
 * mocking support for mocks defined in '''suite scope''' (i.e. outside test case scope).
 *
 * $techniques
 *
 * Specs2 5.x removed "isolated" specifications (each example used to run against its own fresh copy
 * of the specification instance), so a single specification instance - and therefore a single mock -
 * is now shared across all of its examples. Since specs2 may run examples of the same specification
 * concurrently, this trait serializes example execution (via a lock) so that suite-scope mocks can be
 * safely shared without requiring the whole spec to be marked `sequential`, allowing different
 * specifications to still run in parallel.
 *
 * {{{
 * class CoffeeMachineTest extends Specification with SuiteMockFactory {
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
 * 	        coffeeMachine.isOn === true
 * 	        // ...
 * 	        coffeeMachine.powerOff()
 * 	        coffeeMachine.isOn === false
 * 	    }
 *
 * 	    "not turn on the heater when the water container is overfull" in {
 * 	        // examples share a single instance, so expectations set up in suite scope
 * 	        // must be re-defined/consumed per-example rather than relying on isolation
 * 	    }
 * 	}
 * }
 * }}}
 */
trait SuiteMockFactory extends AroundEach with MockContextBase {

  private val lock = new Object

  override def around[T: AsResult](body: => T) = lock.synchronized {
    wrapAsResult[T] { body }
  }
}
