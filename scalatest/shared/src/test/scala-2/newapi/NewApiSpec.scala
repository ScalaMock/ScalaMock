package newapi

import com.paulbutcher.test._
import org.scalamock.stubs.{Stubs, StubNotImplementedError}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import some.other.pkg.SomeOtherClass

import scala.language.higherKinds

import scala.reflect.ClassTag
import scala.util.{Failure, Try}

class NewApiSpec extends AnyFunSpec with Matchers with Stubs {

  it("not compile with other return type for methods without params") {
    val m = stub[TestTrait]
    "(() => m.nullary).returns(1)" shouldNot compile
  }

  it("not compile with other return type for methods with one param") {
    val m = stub[TestTrait]
    "(m.oneParam _).returns((x: Int) => false)" shouldNot compile
  }

  it("not compile with other arg type for methods with one param") {
    val m = stub[TestTrait]
    "(m.oneParam _).returns((x: String) => 1)" shouldNot compile
  }

  it("not compile with other arg type for methods with two params") {
    val m = stub[TestTrait]
    "(m.twoParams _).returns((x: Int, x: String) => 1.toString)" shouldNot compile
  }

  it("not compile with other return type for methods with two params") {
    val m = stub[TestTrait]
    "(m.twoParams _).returns((x: Int, x: Double) => 1)" shouldNot compile
  }

  it("cope with methods without params") {
    val m = stub[TestTrait]
    (() => m.nullary).returnsWith("a return value")

    m.nullary shouldBe "a return value"
  }

  it("cope with infix operators") {
    val m1 = stub[TestTrait]
    val m2 = stub[TestTrait]
    val m3 = stub[TestTrait]
    (m1.+ _).returns(_ => m3)

    m1 + m2 shouldBe m3
  }

  it("cope with curried methods") {
    trait Curried {
      def curried(x: String)(y: String): Int

      def curried2(x: String, y: Int)(z: String): Int

      def curried3(x: String)(y: Int, z: String): Int
    }

    val bar = stub[Curried]

    (bar.curried(_: String)(_: String)).returns {
      case ("1", "2") => 1
      case _ => 2
    }

    (bar.curried2(_: String, _: Int)(_: String)).returns {
      case ("1", 1, "2") => 1
      case _ => 2
    }

    (bar.curried3(_: String)(_: Int, _: String)).returns {
      case ("1", 1, "2") => 1
      case _ => 2
    }

    bar.curried("1")("2") shouldBe 1
    bar.curried("1")("3") shouldBe 2
    bar.curried2("1", 1)("2") shouldBe 1
    bar.curried3("1")(1, "2") shouldBe 1
    (bar.curried(_: String)(_: String)).times shouldBe 2
    (bar.curried(_: String)(_: String)).times(("1", "2")) shouldBe 1
    (bar.curried(_: String)(_: String)).calls shouldBe List(("1", "2"), ("1", "3"))
    (bar.curried2(_: String, _: Int)(_: String)).times shouldBe 1
    (bar.curried2(_: String, _: Int)(_: String)).calls shouldBe List(("1", 1, "2"))
    (bar.curried3(_: String)(_: Int, _: String)).times shouldBe 1
    (bar.curried3(_: String)(_: Int, _: String)).calls shouldBe List(("1", 1, "2"))
  }

  it("cope with methods with repeated parameters") {
    trait Repeated {
      def repeated(x: String*): String

      def repeated2(x: Int, y: String*): String

      def repeated3(x: String*)(y: String*): String
    }

    val bar = stub[Repeated]
    val repeated = stubbed(bar.repeated((_: Seq[String]): _*))
    val repeated2 = stubbed(bar.repeated2(_: Int, (_: Seq[String]): _*))
    val repeated3 = stubbed(bar.repeated3((_: Seq[String]): _*)((_: Seq[String]): _*))

    repeated.returns {
      case Seq("1", "2") => "1"
      case _ => "2"
    }

    bar.repeated("1", "2") shouldBe "1"
    repeated.times shouldBe 1

    repeated2.returns {
      case (1, Seq("1", "2")) => "1"
      case _ => "2"
    }

    bar.repeated2(1, "1", "2") shouldBe "1"
    repeated2.calls shouldBe List((1, Seq("1", "2")))

    repeated3.returns {
      case (Seq("1"), Seq("1", "2")) => "1"
      case _ => "2"
    }

    bar.repeated3("1")("1", "2") shouldBe "1"

  }

  it("cope with methods where Seq[T] is the last parameter") {
    trait ClassWithSeqTParam {
      def run(ints: Seq[Int]): Int
    }

    val m = stub[ClassWithSeqTParam]

    (m.run _).returns {
      case Seq(1, 2, 3) => 100
      case Seq() => 300
      case _ => 200
    }

    m.run(Seq(1, 2, 3)) shouldBe 100
    m.run(Seq(5, 55)) shouldBe 200
    m.run(Seq()) shouldBe 300
  }

  it("cope with methods with implicit parameters") {
    implicit val double: Double = 1.23

    trait TraitWithImplicit {
      def implicitParam(x: Int)(implicit y: Double): String
    }

    val m = stub[TraitWithImplicit]

    val implicitParam = stubbed(m.implicitParam(_: Int)(_: Double))

    implicitParam.returns {
      case (42, 1.23) => "it works"
      case _ => ""
    }

    m.implicitParam(42) shouldBe "it works"
  }

  it("cope with references to another package") {
    val m = stub[TestTrait]
    val x = new SomeOtherClass
    (m.referencesSomeOtherPackage _).returns(x => x)
    assertResult(x) {
      m.referencesSomeOtherPackage(x)
    }
  }

  it("cope with upper bound in another package") {
    val m = stub[TestTrait]
    val x = new SomeOtherClass
    (m.otherPackageUpperBound(_: SomeOtherClass)).returns(x => x)
    assertResult(x) {
      m.otherPackageUpperBound(x)
    }
  }

  it("cope with explicit references to another package") {
    val m = stub[TestTrait]
    val x = new yet.another.pkg.YetAnotherClass
    (m.explicitPackageReference _).returns(x => x)
    assertResult(x) {
      m.explicitPackageReference(x)
    }
  }

  it("cope with upper bound in an explictly referenced package") {
    val m = stub[TestTrait]
    val x = new yet.another.pkg.YetAnotherClass
    (m.explicitPackageUpperBound(_: yet.another.pkg.YetAnotherClass)).returns(x => x)

    assertResult(x) {
      m.explicitPackageUpperBound(x)
    }
  }

  it("cope with a val") {
    val m = stub[TestTrait]
    assertResult(null) {
      m.aVal
    }
  }

  it("cope with a non-abstract val") {
    val m = stub[TestTrait]
    assertResult("foo") {
      m.concreteVal
    }
  }

  it("cope with a function val") {
    val m = stub[TestTrait]
    assertResult(null) {
      m.fnVal
    }
  }

  it("cope with non-abstract methods") {
    val m = stub[TestTrait]
    (m.withImplementation _).returns {
      case 42 => 1234
      case _ => 1
    }

    assertResult(1234) {
      m.withImplementation(42)
    }
  }


  it("mock an embeddded trait") {
    val m = stub[TestTrait]
    val e = stub[m.Embedded]
    (() => m.referencesEmbedded()).returnsWith(e)
    assertResult(e) {
      m.referencesEmbedded()
    }
  }


  it("handle projected types correctly") {
    val m = stub[TestTrait]
    val e = stub[m.Embedded]
    val o = stub[m.ATrait]
    val i = stub[e.ATrait]
    (() => e.innerTraitProjected()).returnsWith(i)
    (() => e.outerTraitProjected()).returnsWith(o)
    assertResult(o) {
      e.outerTraitProjected()
    }
    assertResult(i) {
      e.innerTraitProjected()
    }
  }

  it("handle path-dependent types correctly") {
    val m = stub[TestTrait]
    val e = stub[m.Embedded]
    val o = stub[m.ATrait]
    val i = stub[e.ATrait]
    (() => e.innerTrait()).returnsWith(i)
    (() => e.outerTrait()).returnsWith(o)
    assertResult(o) {
      e.outerTrait()
    }
    assertResult(i) {
      e.innerTrait()
    }
  }

  it("cope with upper bounds") {
    val m = stub[TestTrait]
    (m.upperBound _).returns {
      case (42, "foo") => 2
      case _ => 1
    }
    assertResult(2) {
      m.upperBound((42, "foo"))
    }
  }

  it("cope with lower bounds") {
    val m = stub[TestTrait]
    (m.lowerBound _).returns {
      case ((1, 2), List()) => "it works"
      case _ => "not works"
    }

    assertResult("it works") {
      m.lowerBound((1, 2), List[Product]())
    }
  }

  it("mock a polymorphic trait") {
    val m = stub[PolymorphicTrait[String]]
    (m.method[Double] _).returns {
      case (42, "foo", 1.23) => "a return value"
      case _ => "'as;d"
    }

    assertResult("a return value") {
      m.method(42, "foo", 1.23)
    }
  }

  it("handle path-dependent polymorphic types correctly") {
    val m = stub[PolymorphicTrait[String]]
    val e = stub[m.Embedded[Double]]
    val o = stub[m.ATrait[String, Double]]
    val i = stub[e.ATrait[String, Double]]

    (e.innerTrait _).returns {
      case ("foo", 1.23) => i
    }

    (e.outerTrait _).returns {
      case ("bar", 4.56) => o
    }

    assertResult(o) {
      e.outerTrait("bar", 4.56)
    }
    assertResult(i) {
      e.innerTrait("foo", 1.23)
    }
  }

  it("mock a class") {
    val m = stub[TestClass]
    (m.m _).returns {
      case (42, "foo") => (123, "bar")
      case _ => (1, "ray")
    }

    assertResult((123, "bar")) {
      m.m(42, "foo")
    }
  }

  it("mock a specialized class [Int]") {
    val m1x1 = stub[SpecializedClass[Int]]
    (m1x1.identity _).returns {
      case 42 => 43
    }

    assertResult(43) {
      m1x1.identity(42)
    }
  }

  it("mock a specialized class [Int,String]") {
    val m1x2 = stub[SpecializedClass2[Int, String]]
    (m1x2.identity2 _)
      .returns {
        case (42, "43") => (44, "45")
      }

    assertResult((44, "45")) {
      m1x2.identity2(42, "43")
    }

    (m1x2.identity _).returns {
      case 42 => 43
    }

    assertResult(43) {
      m1x2.identity(42)
    }
  }

  it("mock a specialized class [Int,Int]") {
    val m1x3 = stub[SpecializedClass2[Int, Int]]
    (m1x3.identity2 _).returns {
      case (42, 43) => (44, 45)
    }

    assertResult((44, 45)) {
      m1x3.identity2(42, 43)
    }

    (m1x3.identity _).returns {
      case 42 => 43
    }

    assertResult(43) {
      m1x3.identity(42)
    }
  }

  it("mock a specialized class [String]") {
    val m1x5 = stub[SpecializedClass[String]]
    (m1x5.identity _).returns {
      case "one" => "four"
    }

    assertResult("four") {
      m1x5.identity("one")
    }
  }

  it("mock a specialized class [List[String]]") {
    val m2 = stub[SpecializedClass[List[String]]]
    (m2.identity _).returns {
      case List("one", "two", "three") => List("four", "five", "six")
      case _ => Nil
    }

    assertResult(List("four", "five", "six")) {
      m2.identity(List("one", "two", "three"))
    }
  }

  it("allow to be declared as var") { // test for issue #62
    var m = stub[TestTrait]
    (m.oneParam _).returns {
      case 42 => "foo"
    }

    assertResult("foo") {
      m.oneParam(42)
    }
  }

  it("mock Function1[A, B] trait") { // test for issue #69
    val f = stub[Any => Boolean]
    (f.apply _).returns(_ => true)
    f("this is something") shouldBe true
  }

  it("mock methods that need a class tag") {
    case class User(first: String, last: String, enabled: Boolean)

    trait DataProviderComponent {
      def find[T: ClassTag](id: Int): Try[T]
    }

    val provider = stub[DataProviderComponent]

    (provider.find[User](_: Int)(_: ClassTag[User]))
      .returns { case (x, y) => Failure[User](new Exception()) }

    provider.find[User](13) shouldBe a[Failure[_]]
  }


  it("mock class with nonempty default constructor") {
    class TestNonEmptyDefaultConstructor(a: Int, b: String, c: AnyRef, d: Any)(aa: String)
    val m = stub[TestNonEmptyDefaultConstructor]
  }


  it("cope with curried function returning methods") {
    val m = stub[TestTrait]
    (m.curriedFuncReturn _).returns {
      case 10 => (x: Double) => "curried func return method called"
    }

    val partial = m.curriedFuncReturn(10)
    assertResult("curried func return method called") {
      partial(1.23)
    }
  }



  // issue 132
  it("mock a trait which has a final method") {
    trait FinalMethodTrait {
      def somePublicMethod(param: String): Unit

      final def someFinalMethod(param: Int) = "final method"
    }

    val m = stub[FinalMethodTrait] //test will not compile if the test fails (cannot override final member)
    (m.somePublicMethod _).returns(_ => ())
    // next line will cause a runtime error and is not valid
    // m.someFinalMethod _ expects * anyNumberOfTimes()
  }


  it("mock a trait which has a protected method") {
    trait FooTrait {
      def somePublicMethod(param: String): Unit

      protected[newapi] def protectedMethod() = ()

      private[newapi] def privateMethod() = ()
    }

    val m = stub[FooTrait]
    (m.somePublicMethod _).returns(_ => ())

    // next lines will cause a runtime error and are not valid
    // m.privateMethod _ expects() anyNumberOfTimes()
    // m.protectedMethod _ expects() anyNumberOfTimes()
  }

  it("be able to stub notify(Int)") {
    trait HasNotifyMethod {
      def notify(x: Int): Int
    }

    val foo = stub[HasNotifyMethod]
  }

  it("mock constructor arguments") {
    class WithOption(opt: Option[String])
    class WithInt(i: Int)
    class WithString(s: String)
    "stub[WithOption]" should compile
    "stub[WithInt]" should compile
    "stub[WithString]" should compile
  }

  it("mock traits with overloaded methods which have different number of type params") { // test for issue #85
    trait Foo {
      def overloaded[T](x: T): String

      def overloaded(x: String): String
    }

    val fooMock = stub[Foo]
    (fooMock.overloaded[Double] _).returns {
      case 1.0 => "one"
    }

    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded(_: String)).returns {
      case "2" => "two"
    }

    fooMock.overloaded("2") shouldBe "two"
  }

  it("mock traits with overloaded methods which have different number of type params (2)") {
    trait Foo {
      def overloaded[T](x: T): String

      def overloaded[T](x: T, y: String): String
    }

    val fooMock = stub[Foo]

    (fooMock.overloaded[Double]: Double => String).returns {
      case 1.0 => "one"
    }

    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded[Double]: (Double, String) => String)
      .returns {
        case (2.0, "foo") => "two"
      }

    fooMock.overloaded(2.0, "foo") shouldBe "two"
  }

  it("mock traits with overloaded methods which have different number of type params (3)") {
    trait Foo {
      def overloaded[T](x: T): String

      def overloaded[T, U](x: T, y: U): String
    }

    val fooMock = stub[Foo]

    (fooMock.overloaded[Double]: Double => String).returns {
      case 1.0 => "one"
    }
    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded[Double, String]: (Double, String) => String).returns {
      case (2.0, "foo") => "two"
    }

    fooMock.overloaded(2.0, "foo") shouldBe "two"
  }

  it("mock traits with overloaded methods which have different number of type params (4)") {
    trait Foo {
      def overloaded[T](x: T, y: String): String

      def overloaded[T, U](x: T, y: U): String
    }

    val fooMock = stub[Foo]

    (fooMock.overloaded[Double]: (Double, String) => String).returns {
      case (1.0, "foo") => "one"
    }

    fooMock.overloaded(1.0, "foo") shouldBe "one"

    (fooMock.overloaded[String, Double]: (String, Double) => String).returns {
      case ("foo", 2.0) => "two"
    }

    fooMock.overloaded("foo", 2.0) shouldBe "two"
  }

  it("cope with overloaded methods") {
    val m = stub[TestTrait]
    (m.overloaded(_: Int)).returns {
      case 10 => "got an integer"
    }

    (m.overloaded(_: Int, _: Double)).returns {
      case (10, 1.23) => "got two parameters"
    }

    assertResult("got an integer") {
      m.overloaded(10)
    }
    assertResult("got two parameters") {
      m.overloaded(10, 1.23)
    }
  }

  it("cope with polymorphic overloaded methods") {
    val m = stub[TestTrait]
    (m.overloaded[Double] _).returns {
      case 1.23 => "polymorphic method called"
    }

    assertResult("polymorphic method called") {
      m.overloaded(1.23)
    }
  }

  it("choose between polymorphic and non-polymorphic overloaded methods correctly") {
    val m = stub[TestTrait]
    (m.overloaded(_: Int)).returns {
      case 42 => "non-polymorphic called"
    }

    (m.overloaded[Int] _).returns {
      case 42 => "polymorphic called"
    }

    assertResult("non-polymorphic called") {
      m.overloaded(42)
    }
    assertResult("polymorphic called") {
      m.overloaded[Int](42)
    }
  }

  it("mock PrintStream.print(String)") { // test for issue #39
    import java.io.{OutputStream, PrintStream}
    class MockablePrintStream extends PrintStream(stub[OutputStream], false)

    val m = stub[MockablePrintStream]
    (m.print(_: String)).returns(str => ())
    m.print("foo")
  }


  it("handle type aliases correctly") {
    type X = Int
    type Y = X

    class GenericType[T]
    type ConcreteType = GenericType[X]

    class Foo {
      def foo()(y: GenericType[Y]) = 42

      def foo(a: Int)(y: GenericType[Y]) = 42
    }

    val m = stub[Foo]

    (m.foo()(_: ConcreteType)).returns(_ => 42)

    m.foo()(new ConcreteType())
  }


  it("mock intersection type with type parameter from trait") {

    trait B

    trait C

    trait TraitWithGenericIntersection[A] {
      def methodWithGenericIntersection(x: A with B): Unit
    }

    val m = stub[TraitWithGenericIntersection[C]]

    val obj = new B with C {}

    (m.methodWithGenericIntersection _)
      .returns {
        case `obj` => ()
      }

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with right type parameter from method") {

    trait B

    trait C

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A](x: B with A): Unit
    }

    val m = stub[TraitWithGenericIntersection]

    val obj = new B with C {}

    (m.methodWithGenericIntersection[C] _)
      .returns {
        case `obj` => ()
      }

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with both type parameters from method") {

    trait B

    trait C

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B](x: A with B): Unit
    }

    val m = stub[TraitWithGenericIntersection]

    val obj = new B with C {}

    (m.methodWithGenericIntersection[B, C] _)
      .returns {
        case `obj` => ()
      }

    m.methodWithGenericIntersection(obj)
  }


  it("mock intersection type with more then two types from method") {

    trait B

    trait C

    trait D

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B, C](x: A with B with C): Unit
    }

    val m = stub[TraitWithGenericIntersection]

    val obj = new B with C with D {}

    (m.methodWithGenericIntersection[B, C, D] _)
      .returns {
        case `obj` => ()
      }

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with more then two types from method, one of witch is stable") {

    trait B

    trait C

    trait D

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B](x: A with D with B): Unit
    }

    val m = stub[TraitWithGenericIntersection]

    val obj = new B with C with D {}

    (m.methodWithGenericIntersection[B, C] _).returns {
      case `obj` => ()
    }

    m.methodWithGenericIntersection(obj)
  }


  it("mock intersection return type") {

    trait A

    trait B

    trait TraitWithIntersectionReturnType {

      def methodWithIntersectionReturnType[T](): A with T
    }

    val m = stub[TraitWithIntersectionReturnType]

    val obj = new A with B {}

    (() => m.methodWithIntersectionReturnType[B]()).returnsWith(obj)

    m.methodWithIntersectionReturnType[B]() shouldBe obj
  }

  it("mock methods returning function") {
    trait Test {
      def method(x: Int): Int => String
    }

    val m = stub[Test]

    (m.method _).returns(_ => _ => "f")
    m.method(1)(0) shouldBe "f"

  }

  trait Vars {
    var aVar: Int = _
    var concreteVar = "foo"
  }

  it("mock traits with vars") {
    val m = stub[Vars]
    m.aVar = 6
    m.concreteVar = "bar"
  }

  class A extends B with D

  trait B extends C {
    def foo(): Int = 1

    def bar[T](seq: Seq[T]): Seq[String] = seq.map(_.toString)

    override def baz(a: String, b: Int): Int = a.size + b
  }

  trait C {
    def foo(): Int

    def bar[T](seq: Seq[T]): Seq[String]

    def baz(a: String, b: Int): Int = (a.size * 2) + b
  }

  trait D extends C {
    abstract override def foo(): Int = super.foo() * 2

    abstract override def bar[T](seq: Seq[T]): Seq[String] = "first" +: super.bar(seq) :+ "last"

    abstract override def baz(a: String, b: Int): Int = super.baz(a, b) + 1
  }

  it("permit mocking classes build with stackable trait pattern") {
    val mockedClass = stub[A]
    (() => mockedClass.foo()).returnsWith(42)
    (mockedClass.bar _).returns(_ => Seq("a", "b", "c"))
    (mockedClass.baz _).returns {
      case ("A", 1) => 2
    }

    mockedClass.foo() shouldBe 42
    mockedClass.bar(Seq(1, 2, 3)) shouldBe Seq("a", "b", "c")
    mockedClass.baz("A", 1) shouldBe 2
  }

  trait Bar

  case class Baz(s: String) extends Bar

  trait Foo {
    def p[T <: Bar](gen: Seq[T], t: Seq[T] => Seq[String]): Seq[String] = t(gen)

    def q[T <: Bar](gen: Seq[T]): Seq[String] = gen.map(_.toString)
  }

  it("permit mocking a method that takes a function w/ input parameterised by fn type param") {
    val mockedTrait = stub[Foo]
    (mockedTrait.p[Baz](_: Seq[Baz], _: Seq[_] => Seq[String])).returns {
      case (Seq(Baz("one")), _) => Seq("one")
    }

    (mockedTrait.q[Baz](_: Seq[Baz])).returns {
      case Seq(Baz("one")) => Seq("one")
    }

    mockedTrait.p(Seq(Baz("one")), (_: Seq[Baz]).map(_.toString)) shouldBe Seq("one")
    mockedTrait.q(Seq(Baz("one"))) shouldBe Seq("one")
  }

  trait Command {
    type Answer
    type AnswerConstructor[A]
  }

  case class IntCommand() extends Command {
    override type Answer = Int
    override type AnswerConstructor[A] = Option[A]
  }

  val cmd = IntCommand()

  trait PathDependent {

    def call0[T <: Command](cmd: T): cmd.Answer

    def call1[T <: Command](x: Int)(cmd: T): cmd.Answer

    def call2[T <: Command](y: String)(cmd: T)(x: Int): cmd.Answer

    def call3[T <: Command](cmd: T)(y: String)(x: Int): cmd.Answer

    def call4[T <: Command](cmd: T): Option[cmd.Answer]

    //def call5[T <: Command](cmd: T)(x: cmd.Answer): Unit

    def call6[T <: Command](cmd: T): cmd.AnswerConstructor[Int]

    //def call7[T <: Command](cmd: T)(x: cmd.AnswerConstructor[String])(y: cmd.Answer): Unit
  }


  it("path dependent in return type") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call0[IntCommand] _).returns(_ => 5)

    pathDependent.call0(cmd) shouldBe 5
  }

  it("path dependent in return type and parameter in last parameter list") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call1(_: Int)(_: IntCommand)).returns(_ => 5)

    pathDependent.call1(5)(cmd) shouldBe 5
  }
  

  it("path dependent in return type and parameter in middle parameter list ") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call2(_: String)(_: IntCommand)(_: Int)).returns(_ => 5)

    pathDependent.call2("5")(cmd)(5) shouldBe 5
  }

  
  it("path dependent in return type and parameter in first parameter list ") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call3(_: IntCommand)(_: String)(_: Int)).returns(_ => 5)

    pathDependent.call3(cmd)("5")(5) shouldBe 5
  }

  
  it("path dependent in tycon return type") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call4[IntCommand] _).returns {
      case `cmd` => Some(5)
      case _ => None
    }

    assert(pathDependent.call4(cmd).contains(5))
  }
  
/*
  it("path dependent in parameter list") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call5(_: IntCommand)(_: Int)).returns(_ => ())

    assert(pathDependent.call5(cmd)(5) == ())
  }
    */

  it("path dependent tycon in return type") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call6[IntCommand] _).returns(_ => Some(5))

    assert(pathDependent.call6(cmd).contains(5))
  }
/*
  it("path dependent tycon in parameter list") {
    val pathDependent = stub[PathDependent]

    (pathDependent.call7[IntCommand](_: IntCommand)(_: Option[String])(_: Int))
      .returns(_ => ())

    assert(pathDependent.call7(cmd)(Some("5"))(6) == ())
  }*/

  it("compile without args") {
    class ContextBounded[T: ClassTag] {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("compile with args") {
    class ContextBounded[T: ClassTag](x: Int) {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("compile with provided explicitly type class") {
    class ContextBounded[T](x: ClassTag[T]) {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("mock type constructor arguments") {
    class WithTC[TC[_]](tc: TC[Int])
    type ID[A] = A
    val foo = stub[WithTC[List]]
    val bar = stub[WithTC[ID]]
  }

  it("mock generic arguments") {
    class WithGeneric[T](t: T)

    val foo = stub[WithGeneric[String]]
    val bar = stub[WithGeneric[Int]]
  }

  it("mock type constructor context bounds") {
    trait Async[F[_]]
    class A[F[_] : Async](val b: B[F])
    class B[F[_] : Async](val c: C[F])
    trait C[F[_]]

    val foo = stub[A[List]]
    val bar = stub[B[List]]
    val baz = stub[C[List]]
  }

  case class CaseClass(a: Int)

  class ClassHavingMethodsWithDefaultParams() {
    def withOneDefaultParam(a: String, b: String = "default"): String = "?"

    def withTwoDefaultParams(a: String, b: String = "default", c: Int = 42): String = "?"
  }

  trait TraitHavingMethodsWithDefaultParams {
    def withAllDefaultParams(a: String = "default", b: CaseClass = CaseClass(42)): String

    def withDefaultParamAndTypeParam[T](a: String = "default", b: Int = 5): T
  }

  it("stub class methods with one default parameter") {
    val m = stub[ClassHavingMethodsWithDefaultParams]

    (m.withOneDefaultParam _).returns {
      case ("a", "default") => "one"
      case ("a", "other") => "two"
      case _ => "three"
    }

    m.withOneDefaultParam("a") shouldBe "one"
    m.withOneDefaultParam("a", "default") shouldBe "one"
    m.withOneDefaultParam("a", "other") shouldBe "two"

    intercept[StubNotImplementedError] {
      m.withTwoDefaultParams("x", "y") // not stubbed
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      startWith("Implementation is missing for [") and
        include("ClassHavingMethodsWithDefaultParams.withTwoDefaultParams(a: String, b: String, c: Int)") and
        endWith("] and argument [(x,y,42)]")
    }
  }

  it("stub class methods with two default parameters") {
    val m = stub[ClassHavingMethodsWithDefaultParams]

    (m.withTwoDefaultParams _).returns {
      case ("a", "default", 42) => "one"
      case ("a", "other", 99) => "two"
      case _ => "three"
    }

    m.withTwoDefaultParams("a") shouldBe "one"
    m.withTwoDefaultParams("a", "default") shouldBe "one"
    m.withTwoDefaultParams("a", "default", 42) shouldBe "one"
    m.withTwoDefaultParams("a", "other", 99) shouldBe "two"

    intercept[StubNotImplementedError] {
      m.withOneDefaultParam("x") // not stubbed
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      startWith("Implementation is missing for [") and
        include("ClassHavingMethodsWithDefaultParams.withOneDefaultParam(a: String, b: String)") and
        endWith("] and argument [(x,default)]")
    }
  }

  it("stub trait methods with type param and default parameters") {
    val m = stub[TraitHavingMethodsWithDefaultParams]

    (m.withDefaultParamAndTypeParam[Int] _).returns {
      case ("default", 5) => 5
      case _ => 6
    }

    m.withDefaultParamAndTypeParam[Int]("default", 5) shouldBe 5
    m.withDefaultParamAndTypeParam[Int]("defaul", 5) shouldBe 6

    intercept[StubNotImplementedError] {
      m.withAllDefaultParams() // not stubbed
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      startWith("Implementation is missing for [") and
        include("TraitHavingMethodsWithDefaultParams.withAllDefaultParams(a: String, b: NewApiSpec.this.CaseClass)") and
        endWith("] and argument [(default,CaseClass(42))]")
    }
  }

  it("returnsWhen cope with 1-param method") {
    val m = stub[TestTrait]
    (m.oneParam _).returnsWhen {
      case 42 => "the answer to everything"
    }

    m.oneParam(42) shouldBe "the answer to everything"

    intercept[StubNotImplementedError] {
      m.oneParam(100)
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      startWith("Implementation is missing for [") and
        include("TestTrait.oneParam(x: Int)") and
        endWith("] and argument [100]")
    }
  }

  it("returnsWhen cope with 2-param method") {
    val m = stub[TestTrait]
    (m.twoParams _).returnsWhen {
      case (1, 2.3) => "nice"
      case (4, _) => "cool"
    }

    m.twoParams(1, 2.3) shouldBe "nice"
    m.twoParams(4, 5.6) shouldBe "cool"

    intercept[StubNotImplementedError] {
      m.twoParams(1, 1.1)
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      // Also note that Double can be formatted differently on JVM and JS.
      startWith("Implementation is missing for [") and
        include("TestTrait.twoParams(x: Int, y: Double)") and
        endWith("] and argument [(1,1.1)]")
    }
  }

  it("returnsWhen cope with curried method") {
    val m = stub[TestTrait]
    (m.curried(_: Int)(_: Double)).returnsWhen {
      case (123, 45.6) => "789"
    }
    m.curried(123)(45.6) shouldBe "789"

    intercept[StubNotImplementedError] {
      m.curried(654)(3.21)
    }.getMessage should {
      // Exact error message varies in Scala 2.12 and 2.13
      // Also note that Double can be formatted differently on JVM and JS.
      startWith("Implementation is missing for [") and
        include("TestTrait.curried(x: Int)(y: Double)") and
        endWith("] and argument [(654,3.21)]")
    }
  }

  it("should mock generic Iterable") {
    val m = stub[Iterable[Int]]
  }

}
