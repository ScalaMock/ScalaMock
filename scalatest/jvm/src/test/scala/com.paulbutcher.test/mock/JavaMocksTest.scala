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

class JavaMocksTest extends IsolatedSpec {
  behavior of "ScalaMock while mocking Java classes and interfaces"

  it should "mock Java generics" in {
    val m = mock[JavaGenericInterface[Int]]
    (m.simpleMethod _) expects ("two") returning 42

    m.simpleMethod("two") shouldBe 42
  }

    it should "mock classes with bridged methods" in {
      class JavaClassWithBridgeMethodExtended extends JavaClassWithBridgeMethod
      val m = mock[JavaClassWithBridgeMethodExtended]
  
      (m.compare _).expects(Integer.valueOf(5)).returning(1)
      (m.compare _).expects(Integer.valueOf(6)).returning(2)
  
      def useBridgeMethod[T](gen: JavaGenericInterface[T], x: T) = {
        gen.compare(x)
      }
  
      assertResult(1) { m.compare(Integer.valueOf(5)) } // calls: int compare(Integer)
      assertResult(2) { useBridgeMethod(m, Integer.valueOf(6)) } // calls: int compare(Object)
    }

  //! TODO - this is going to have to wait for macro types for a proper solution
  //    "cope with Java methods with repeated parameters" in {
  //      withExpectations {
  //        val m = mock[JavaInterface]
  //        (m.repeatedParam _).expects(42, Seq(1.23, 4.56))
  //        m.repeatedParam(42, 1.23, 4.56)
  //      }
  //    }

  it should "mock a Java interface" in {
    val m = mock[JavaInterface]
    (m.m _).expects(42, "foo").returning("a return value")
    assertResult("a return value") { m.m(42, "foo") }
  }

    it should "mock a Polymorhpic Java interface" in { // test for issue #24
      val m = mock[PolymorphicJavaInterface]
      (m.simplePolymorphicMethod[Int] _).expects("foo").returning(44)
      assertResult(44) { m.simplePolymorphicMethod[Int]("foo") }
    }

  it should "mock a Polymorhpic Java interface (type parametrized method parameter)" in {
    val m = mock[PolymorphicJavaInterface]
    val arg = new java.util.ArrayList[String]
    (m.polymorphicMethod[String] _).expects(arg).returning("foo")

    m.polymorphicMethod(arg) shouldBe "foo"
  }

    it should "mock a Java class with an overloaded method (different param count)" in { // test for issue #34
      class JavaClassWithOverloadedMethodExtended extends JavaClassWithOverloadedMethod

      val m = mock[JavaClassWithOverloadedMethodExtended]
      (m.overloadedMethod(_: String)).expects("a").returning("first")
      (m.overloadedMethod(_: String, _: String)).expects("a", "b").returning("second")
  
      m.overloadedMethod("a") shouldBe "first"
      m.overloadedMethod("a", "b") shouldBe "second"
    }
  
    it should "mock a Java class with an overloaded method (the same param count)" in { // test for issue #73
      class JavaClassWithOverloadedMethodExtended extends JavaClassWithOverloadedMethod

      val m = mock[JavaClassWithOverloadedMethodExtended]
      (m.overloadedSameParamCount(_: String)).expects("one").returning("first")
      (m.overloadedSameParamCount(_: Integer)).expects(Integer.valueOf(2)).returning(2)
  
      m.overloadedSameParamCount("one") shouldBe "first"
      m.overloadedSameParamCount(2) shouldBe 2
    }


    it should "mock a Java class with an overloaded method (with primitive param)" in { // test for issue #73
      class JavaClassWithOverloadedMethodExtended extends JavaClassWithOverloadedMethod

      val m = mock[JavaClassWithOverloadedMethodExtended]

      (m.overloadedWithPrimitiveParam(_: String)).expects("one").returning("first")
      (m.overloadedWithPrimitiveParam(_: Int)).expects(2).returning("second")
  
      m.overloadedWithPrimitiveParam("one") shouldBe "first"
      m.overloadedWithPrimitiveParam(2) shouldBe "second"
    }


    it should "mock a Java class with an overloaded method (with type params)" in {
      class JavaClassWithOverloadedMethodExtended extends JavaClassWithOverloadedMethod

      val m = mock[JavaClassWithOverloadedMethodExtended]
      (m.overloadedGeneric(_: String)).expects("one").returning("first")
      (m.overloadedGeneric(_: Integer)).expects(Integer.valueOf(2)).returning("second")
  
      m.overloadedGeneric("one") shouldBe "first"
      m.overloadedGeneric(2) shouldBe "second"
    }

  override def newInstance = new JavaMocksTest
}