package steve

import cats.effect.IOApp
import cats.effect.IO
import org.http4s.ember.client.EmberClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter

object Main extends IOApp.Simple {

  def run: IO[Unit] = EmberClientBuilder.default[IO].build.use { client =>
    given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()
    val exec = ClientSideExecutor.instance[IO](client)
    
    exec.build(Build.empty)
    .flatMap(h => IO.println(s"size = ${h.value.size}").as(h))
    .flatMap(exec.run)
    .flatMap(IO.println)
  }

}
