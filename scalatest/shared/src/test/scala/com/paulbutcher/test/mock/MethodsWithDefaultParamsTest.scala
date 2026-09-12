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

package com.paulbutcher.test.mock

import com.paulbutcher.test._

class MethodsWithDefaultParamsTest extends IsolatedSpec {

  case class CaseClass(a: Int)

  class ClassHavingMethodsWithDefaultParams() {
    def withOneDefaultParam(a: String, b: String = "default"): String = "?"
    def withTwoDefaultParams(a: String, b: String = "default", c: Int = 42): String = "?"
    def withAllDefaultParams(a: String = "default", b: CaseClass = CaseClass(42)): String = "?"
  }

  trait TraitHavingMethodsWithDefaultParams {
    def withAllDefaultParams(a: String = "default", b: CaseClass = CaseClass(42)): String

    def withDefaultParamAndTypeParam[T](a: String = "default", b: Int = 5): T
  }

  behavior of "Mocks"

  they should "mock class methods with one default parameter" in {
    val m = mock[ClassHavingMethodsWithDefaultParams]

    (m.withOneDefaultParam _) expects ("a", "default") returning "one"
    (m.withOneDefaultParam _) expects ("a", "default") returning "two"
    (m.withOneDefaultParam _) expects ("a", "other") returning "three"

    m.withOneDefaultParam("a") shouldBe "one"
    m.withOneDefaultParam("a", "default") shouldBe "two"
    m.withOneDefaultParam("a", "other") shouldBe "three"
  }

  they should "mock class methods with two default parameters" in {
    val m = mock[ClassHavingMethodsWithDefaultParams]

    (m.withTwoDefaultParams _) expects ("a", "default", 42) returning "one"
    (m.withTwoDefaultParams _) expects ("a", "default", 42) returning "two"
    (m.withTwoDefaultParams _) expects ("a", "default", 42) returning "three"
    (m.withTwoDefaultParams _) expects ("a", "other", 99) returning "four"

    m.withTwoDefaultParams("a") shouldBe "one"
    m.withTwoDefaultParams("a", "default") shouldBe "two"
    m.withTwoDefaultParams("a", "default", 42) shouldBe "three"
    m.withTwoDefaultParams("a", "other", 99) shouldBe "four"
  }

  they should "mock class methods with all parameters having a default value" in {
    val m = mock[ClassHavingMethodsWithDefaultParams]

    (m.withAllDefaultParams _) expects ("default", CaseClass(42)) returning "one"
    (m.withAllDefaultParams _) expects ("other", CaseClass(99)) returning "two"

    m.withAllDefaultParams()
    m.withAllDefaultParams("other", CaseClass(99))
  }

  they should "mock trait methods with all parameters having a default value" in {
    val m = mock[TraitHavingMethodsWithDefaultParams]

    (m.withAllDefaultParams _) expects ("default", CaseClass(42)) returning "one"
    (m.withAllDefaultParams _) expects ("other", CaseClass(99)) returning "two"

    m.withAllDefaultParams()
    m.withAllDefaultParams("other", CaseClass(99))
  }

  they should "mock trait methods with type param and default parameters" in {
    val m = mock[TraitHavingMethodsWithDefaultParams]

    (m.withDefaultParamAndTypeParam[Int] _).expects("default", 5).returns(5)

    m.withDefaultParamAndTypeParam[Int]("default", 5) shouldBe 5
  }

  override def newInstance = new MethodsWithDefaultParamsTest
}
