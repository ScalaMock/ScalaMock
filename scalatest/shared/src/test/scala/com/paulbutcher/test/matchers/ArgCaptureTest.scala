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

import com.paulbutcher.test.TestTrait
import org.scalamock.scalatest.MockFactory
import org.scalamock.matchers.ArgCapture._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArgCaptureTest extends AnyFlatSpec with Matchers with MockFactory {
  behavior of "CaptureOne"

  it should "capture arguments - string" in {
    val c1 = CaptureOne[String]()
    c1.value = "foo"
    c1.value should be("foo")
  }

  it should "capture arguments - long" in {
    val c1 = CaptureOne[Long]()
    c1.value = 5329874097912748021L
    c1.value should be(5329874097912748021L)
  }

  it should "only keep the last value" in {
    val c1 = CaptureOne[Int]()
    c1.value = 5
    c1.value = 9
    c1.value should be(9)
  }

  it should "throw if nothing was captured" in {
    val c1 = CaptureOne[Long]()
    an[NoSuchElementException] should be thrownBy { c1.value }
  }

  behavior of "CaptureAll"

  it should "capture many arguments" in {
    val c1 = CaptureAll[String]()
    c1.value = "foo"
    c1.value = "bar"
    c1.value should be ("bar")
    c1.values should be (Seq("foo", "bar"))
  }

  it should "return an empty seq if nothing was captured" in {
    val c1 = CaptureAll[String]()
    an[NoSuchElementException] should be thrownBy { c1.value }
    c1.values should be (empty)
  }

  behavior of "CaptureMatcher"

  it should "capture the arguments of mocks - capture one" in {
    val m = mock[TestTrait]
    val c1 = CaptureOne[Int]()

    (m.oneParam _).expects(capture(c1)).once()
    m.oneParam(42)
    c1.value should be (42)
  }

  it should "capture the arguments of mocks - capture all" in {
    val m = mock[TestTrait]
    val c = CaptureAll[Int]()

    m.oneParam _ expects capture(c) repeat 3
    m.oneParam(99)
    m.oneParam(17)
    m.oneParam(583)
    c.value should be (583)
    c.values should be (Seq(99, 17, 583))
  }

  it should "be able to capture multiple arguments in a call" in {
    val m = mock[TestTrait]
    val c = CaptureAll[Any]()

    (m.twoParams _).expects(capture(c), capture(c)).once()
    m.twoParams(7, 3.0)
    c.values should be (Seq(7, 3.0))
  }

}
