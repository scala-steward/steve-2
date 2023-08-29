package steve

import munit.CatsEffectSuite
import cats.effect.IO
import sttp.tapir.client.http4s.Http4sClientInterpreter
import org.http4s.client.Client
import cats.effect.kernel.Async
import cats.syntax.all.*

class CompatTests extends CatsEffectSuite {

  val goodBuild: Build = Build.empty
  val goodBuildResult: Hash = Hash(List.empty)

  val unknownHash = Hash(List(1))
  val unexpectedFailingHash: Hash = unknownHash
  val unexpectedFailingBuild: Build = Build(Build.Base.EmptyImage, List(Build.Command.Delete("k")))

  val buildInternalError: Throwable = new Exception("build internal error")

  val goodHash: Hash = Hash(List.empty)
  val goodRunResult: SystemState = SystemState(Map.empty)

  val unknownBaseBuild: Build = Build(
    Build.Base.ImageReference(unknownHash),
    Nil,
  )

  val unknownBaseError: Throwable = Build.Error.UnknownBase(unknownHash)

  val exec: Executor[IO] = TestExecutor.instance(
    Map(
      goodBuild -> goodBuildResult.asRight,
      unknownBaseBuild -> unknownBaseError.asLeft,
      unexpectedFailingBuild -> buildInternalError.asLeft,
    ),
    Map(
      goodBuildResult -> goodRunResult.asRight,
      unexpectedFailingHash -> new Exception("run internal error").asLeft,
    ),
  )

  given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()

  val client = ClientSideExecutor.instance[IO](
    Client.fromHttpApp(
      Routing.instance[IO](
        exec
      )
    )
  )

  test("Build image - success") {
    assertIO(
      client.build(goodBuild),
      goodBuildResult,
    )
  }

  test("Build image - unkown base error") {
    assertIO(
      client.build(unknownBaseBuild).attempt,
      unknownBaseError.asLeft,
    )
  }

  test("Build image - unexpected failure") {
    assertIO(
      client.build(unexpectedFailingBuild).attempt,
      GenericServerError("server failed").asLeft,
    )
  }

  test("Run image - success") {
    assertIO(
      client.run(goodHash),
      goodRunResult,
    )
  }

  test("Run hash - unexpected failure") {
    assertIO(
      client.run(unexpectedFailingHash).attempt,
      GenericServerError("server failed").asLeft,
    )
  }
}
