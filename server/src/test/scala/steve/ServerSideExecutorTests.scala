package steve

import munit.CatsEffectSuite

class ServerSideExecutorTests extends CatsEffectSuite {
  val exec = ServerSideExecutor.instance[Either[Throwable, *]]

  test("build and run empty image") {
    assertEquals(
      exec.build(Build.empty).flatMap(exec.run).map(_.all),
      Right(Map.empty),
    )
  }
}
