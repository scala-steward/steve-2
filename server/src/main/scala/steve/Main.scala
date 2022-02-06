package steve

import com.comcast.ip4s.port
import com.comcast.ip4s.host
import cats.effect.IOApp
import org.http4s.ember.server.EmberServerBuilder
import cats.effect.IO
import org.http4s.implicits._
import sttp.tapir.server.ServerEndpoint

object Main extends IOApp.Simple {

  val executor = ServerSideExecutor.instance[IO]

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Routing.instance[IO](executor))
      .build
      .useForever

}
