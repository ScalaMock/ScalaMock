package newapi

import org.scalamock.stubs.Stubs
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*

enum UserStatus:
  case Normal, Blocked

enum FailedAuthResult:
  case UserNotFound, UserNotAllowed, WrongPassword

case class User(id: Long, status: UserStatus)

trait UserService:
  def findUser(userId: Long): Future[Option[User]]

trait PasswordService:
  def checkPassword(id: Long, password: String): Future[Boolean]

class UserAuthService(
   userService: UserService,
   passwordService: PasswordService
):
  def authorize(id: Long, password: String): Future[Either[FailedAuthResult, Unit]] =
    userService.findUser(id).flatMap:
      case None =>
        Future.successful(Left(FailedAuthResult.UserNotFound))

      case Some(user) if user.status == UserStatus.Blocked =>
        Future.successful(Left(FailedAuthResult.UserNotAllowed))

      case Some(user) =>
        passwordService.checkPassword(id, password).map {
          case true => Right(())
          case false => Left(FailedAuthResult.WrongPassword)
        }


class UserAuthServiceSpec extends AnyFunSpec, Matchers, Stubs, ScalaFutures, PatienceConfiguration:
  given PatienceConfig = PatienceConfig(1.second, 100.millis)


  val unknownUserId = 0
  val user = User(1, UserStatus.Normal)
  val blockedUser = User(2, UserStatus.Blocked)
  val validPassword = "valid"
  val invalidPassword = "invalid"

  case class Verify(
    passwordCheckedTimes: Option[Int]
  )

  testCase(
    description = "error if user not found",
    id = unknownUserId,
    password = validPassword,
    expectedResult = Left(FailedAuthResult.UserNotFound),
    verify = Verify(passwordCheckedTimes = Some(0))
  )

  testCase(
    description = "error if user is blocked",
    id = blockedUser.id,
    password = validPassword,
    expectedResult = Left(FailedAuthResult.UserNotAllowed),
    verify = Verify(passwordCheckedTimes = Some(0))
  )

  testCase(
    description = "error if password is invalid",
    id = user.id,
    password = invalidPassword,
    expectedResult = Left(FailedAuthResult.WrongPassword),
    verify = Verify(passwordCheckedTimes = Some(1))
  )

  testCase(
    description = "password valid",
    id = user.id,
    password = validPassword,
    expectedResult = Right(()),
    verify = Verify(passwordCheckedTimes = Some(1))
  )

  def testCase(
    description: String,
    id: Long,
    password: String,
    expectedResult: Either[FailedAuthResult, Unit],
    verify: Verify
  ) = it(description) {
    val userService = stub[UserService]
    val passwordService = stub[PasswordService]
    val userAuthService = UserAuthService(userService, passwordService)

    userService.findUser.returns:
      case user.id => Future.successful(Some(user))
      case blockedUser.id => Future.successful(Some(blockedUser))
      case _ => Future.successful(None)

    passwordService.checkPassword.returns:
      case (_, password) => Future.successful(password == validPassword)

    val result = for
      result <- userAuthService.authorize(id, password)
    yield (result, Option(passwordService.checkPassword.times))

    result.futureValue
      .shouldBe((expectedResult, verify.passwordCheckedTimes))
  }


