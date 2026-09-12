package org.scalamock.specs2

import org.specs2.execute.AsResult
import org.specs2.specification.Around

/**
 * Fixture context that should be created per test-case basis
 *
 * $techniques
 *
 * '''Fixture contexts''' are more flexible and are recommened for complex test suites where single
 * set of fixtures does not fit all test cases.
 *
 * ==Basic usage==
 *
 * For simple test cases it's enough to run test case in new `MockContext` scope.
 *
 * {{{
 * class BasicCoffeeMachineTest extends Specification {
 *
 *  	"CoffeeMachine" should {
 *  	     "not turn on the heater when the water container is empty" in new MockContext {
 *  	         val waterContainerMock = mock[WaterContainer]
 *  	         (waterContainerMock.isOverfull _).expects().returning(true)
 *  	         // ...
 *  	     }
 *
 *  	     "not turn on the heater when the water container is overfull" in new MockContext {
 *  	         val waterContainerMock = mock[WaterContainer]
 *  	         // ...
 *  	     }
 *  	}
 * }
 * }}}
 *
 * ==Complex fixture contexts==
 *
 * When multiple test cases need to work with the same mocks (and more generally - the same
 * fixtures: files, sockets, database connections, etc.) you can use fixture contexts.
 *
 * {{{
 * class CoffeeMachineTest extends Specification {
 *
 * 	trait Test extends MockContext { // fixture context
 * 	    // shared objects
 * 	    val waterContainerMock = mock[WaterContainer]
 * 	    val heaterMock = mock[Heater]
 * 	    val coffeeMachine = new CoffeeMachine(waterContainerMock, heaterMock)
 *
 * 	    // test setup
 * 	    coffeeMachine.powerOn()
 * 	}
 *
 * 	// you can extend and combine fixture-contexts
 * 	trait OverfullWaterContainerTest extends Test {
 * 	    // you can set expectations and use mocks in fixture-context
 * 	    (waterContainerMock.isEmpty _).expects().returning(true)
 *
 * 	    // and define helper functions
 * 	    def complexLogic() {
 * 	        coffeeMachine.powerOff()
 * 	        // ...
 * 	    }
 * 	}
 *
 * 	"CoffeeMachine" should {
 * 	     "not turn on the heater when the water container is empty" in new MockContext {
 * 	         val heaterMock = mock[Heater]
 * 	         val waterContainerMock = mock[WaterContainer]
 * 	         val coffeeMachine = new CoffeeMachine(waterContainerMock, heaterMock)
 * 	         (waterContainerMock.isOverfull _).expects().returning(true)
 * 	         // ...
 * 	     }
 *
 * 	     "not turn on the heater when the water container is overfull" in new OverfullWaterContainerTest {
 * 	         // ...
 * 	         complexLogic()
 * 	     }
 * 	}
 * }
 * }}}
 */
trait MockContext extends MockContextBase with Around {

  override def around[T: AsResult](body: => T) = {
    wrapAsResult[T] { body }
  }
}
