package steve

import cats.effect.MonadCancelThrow
import org.http4s.client.Client
import org.http4s.implicits.*
import cats.implicits.*
import sttp.tapir.client.http4s.Http4sClientInterpreter
import sttp.tapir.Endpoint
import sttp.model.StatusCodes
import sttp.model.StatusCode

object ClientSideExecutor {

  def instance[F[_]: Http4sClientInterpreter: MonadCancelThrow](
    client: Client[F]
  )(
    using fs2.Compiler[F, F]
  ): Executor[F] =
    new Executor {

      private def runEndpoint[I, E <: Throwable, O](
        endpoint: Endpoint[Unit, I, E, O, Any],
        input: I,
      ): F[O] = {
        val (req, handler) = summon[Http4sClientInterpreter[F]]
          .toRequestThrowDecodeFailures(endpoint, Some(uri"http://localhost:8080"))
          .apply(input)
        client
          .run(req)
          .use {
            case resp if resp.status.code == StatusCode.InternalServerError.code =>
              resp
                .bodyText
                .compile
                .string
                .flatMap(io.circe.parser.decode[GenericServerError](_).liftTo[F])
                .flatMap(_.raiseError[F, O])
            case resp => handler(resp).rethrow
          }
      }

      def build(build: Build): F[Hash] = runEndpoint(protocol.build, build)

      def run(run: Hash): F[SystemState] = runEndpoint(protocol.run, run)
    }

}
