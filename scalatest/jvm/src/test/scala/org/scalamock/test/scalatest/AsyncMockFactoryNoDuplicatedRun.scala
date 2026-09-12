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

package org.scalamock.test.scalatest

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

/**
  * Test to ensure AsyncMockFactory only run test once
  */
class AsyncMockFactoryNoDuplicatedRun extends AsyncFlatSpec with Matchers with AsyncMockFactory {
  trait TestTrait {
    def mockMethod(): Int
  }

  class ClassUnderTest(protected val testTrait: TestTrait) {
    def methodUnderTest(): Future[Int] = {
      TestCounter.alreadyRun = TestCounter.alreadyRun + 1
      Future(testTrait.mockMethod())
    }
  }

  object TestCounter {
    var alreadyRun: Int = 0
  }

  "AsyncMockFactory" should "run test case provided successfully" in {
    val mockTrait = mock[TestTrait]
    val returnVal = 100
    (() => mockTrait.mockMethod()).expects().returning(returnVal)
    val classUnderTest = new ClassUnderTest(mockTrait)
    classUnderTest.methodUnderTest().map(_ shouldBe returnVal)
  }

  "AsyncMockFactory" should "have run test case provided only once" in {
    Future(TestCounter.alreadyRun shouldBe 1)
  }
}
