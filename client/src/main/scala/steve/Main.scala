package steve

import cats.effect.IOApp
import cats.effect.IO
import cats.syntax.*
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  val logger = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .withRetryPolicy((_, _, _) => None)
    .build
    .use { client =>
      given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()
      val exec = ClientSideExecutor.instance[IO](client)

      logger.info("Building base image") *>
        exec
          .build(Build.empty)
          .flatTap(hash => logger.info(s"Built image with hash $hash"))
          .flatMap(exec.run)
          .flatMap(result => logger.info(s"Run image with result $result"))
    }
    .orElse {
      logger.error("Unhandled error")
    }

}
