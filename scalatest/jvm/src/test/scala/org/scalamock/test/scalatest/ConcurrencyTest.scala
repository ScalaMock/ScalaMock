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

import org.scalamock.scalatest.MockFactory
import org.scalamock.test.mockable.TestTrait
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ConcurrencyTest extends AnyFlatSpec with MockFactory {
  behavior of "ScalaMock"

  it should "work with Futures" in {
    val s = stubFunction[Int]
    s.when().returns(1)
    assertResult(1) { Await.result(Future { s() }, 10.seconds) }
    s.verify().once()
  }

  it should "work with concurrent mock access" in {
    val m = mock[TestTrait]
    (m.oneParamMethod _).expects(42).repeated(500000).returning("a")

    val futures = (1 to 500000).map { _ =>
      Future { m.oneParamMethod(42) }
    }

    futures.foreach(future => Await.result(future, 10.seconds))
  }
}
