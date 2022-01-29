package steve

import com.comcast.ip4s.port
import com.comcast.ip4s.host
import cats.effect.IOApp
import org.http4s.ember.server.EmberServerBuilder
import cats.effect.IO
import org.http4s.implicits._
import sttp.tapir.server.ServerEndpoint

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp {
        import sttp.tapir.server.http4s.Http4sServerInterpreter

        val exec = ServerSideExecutor.instance[IO]

        val se: List[ServerEndpoint[Any, IO]] = List(
          protocol.build.serverLogicSuccess(exec.build),
          protocol.run.serverLogicSuccess(exec.run),
        )
        Http4sServerInterpreter[IO]().toRoutes(se).orNotFound
      }
      .build
      .useForever

}
