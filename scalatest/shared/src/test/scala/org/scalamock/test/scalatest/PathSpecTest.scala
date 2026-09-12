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

import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.PathMockFactory
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funspec
import org.scalatest.matchers.should.Matchers

class PathSpecTest extends funspec.PathAnyFunSpec with Matchers with PathMockFactory {
  override def newInstance = new PathSpecTest

  describe("PathSpec") {
    val mockFun = mockFunction[String, Unit]
    mockFun expects "top-level"

    describe("can handle stackable expectations") {
      mockFun expects "mid-level"
      mockFun("top-level")

      it("does not throw exception if all expectations are met") {
        mockFun("mid-level")
        verifyExpectations()
      }

      it("fails if mid-level expectation is not met") {
        an[TestFailedException] should be thrownBy verifyExpectations()
      }
    }

    it("fails if top-level expectation is not met") {
      an[TestFailedException] should be thrownBy verifyExpectations()
    }
  
    it("can have expectations checked at the end of root suite") {
      val mockFun: MockFunction1[String, Unit] = mockFunction[String, Unit]("mockFun")
      mockFun expects "bottom-level"
      val caught = intercept[TestFailedException] {
        verifyExpectations()
      }
      caught.getMessage() should include("mockFun(bottom-level) once (never called - UNSATISFIED)")
    }
    
  }

  describe("PathSpec") {
    val mockFun = mockFunction[String, Unit]

    it("throws an exception with good error message if the mock is called after expectations are verified") {
      verifyExpectations()
      val thrown = the [RuntimeException] thrownBy(mockFun("called after verification"))
      thrown should not be a[NullPointerException]
      thrown.getMessage should include ("have expectations been verified already?")
    }
  }
}
