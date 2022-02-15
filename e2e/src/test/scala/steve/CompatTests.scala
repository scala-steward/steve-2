package steve

import munit.CatsEffectSuite
import cats.effect.IO
import sttp.tapir.client.http4s.Http4sClientInterpreter
import org.http4s.client.Client
import cats.effect.kernel.Async
import cats.syntax.all.*

class CompatTests extends CatsEffectSuite {

  def testExecutor(
    buildImpl: Map[Build, Either[Throwable, Hash]],
    runImpl: Map[Hash, Either[Throwable, SystemState]],
  ): Executor[IO] =
    new Executor[IO] {
      override def build(build: Build): IO[Hash] = buildImpl(build).liftTo[IO]
      override def run(hash: Hash): IO[SystemState] = runImpl(hash).liftTo[IO]
    }

  val goodBuild: Build = Build.empty
  val goodBuildResult: Hash = Hash(List.empty)

  val unexpectedFailingHash: Hash = Hash(List(1))
  val unexpectedFailingBuild: Build = Build(Build.Base.EmptyImage, List(Build.Command.Delete("k")))

  val buildInternalError: Throwable = new Exception("build internal error")

  val goodHash: Hash = Hash(List.empty)
  val goodRunResult: SystemState = SystemState(Map.empty)

  val exec: Executor[IO] = testExecutor(
    Map(
      goodBuild -> goodBuildResult.asRight,
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

  test("Build empty image successfully") {
    assertIO(
      client.build(goodBuild),
      goodBuildResult,
    )
  }

  test("Build image - unexpected failure") {
    assertIO(
      client.build(unexpectedFailingBuild).attempt,
      GenericServerError("server failed").asLeft,
    )
  }

  test("Run empty image successfully") {
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
