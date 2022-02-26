package steve

import org.http4s.HttpApp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.ServerEndpoint
import cats.effect.kernel.Async
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.json.circe.*
import sttp.model.StatusCode
import sttp.tapir.*

object Routing {

  def instance[F[_]: Async](exec: Executor[F]): HttpApp[F] = {

    val endpoints: List[ServerEndpoint[Any, F]] = List(
      protocol.build.serverLogicRecoverErrors(exec.build),
      protocol.run.serverLogicSuccess(exec.run),
    )
    Http4sServerInterpreter[F](
      Http4sServerOptions
        .customInterceptors[F, F]
        .exceptionHandler { _ =>
          Some(
            ValuedEndpointOutput(
              jsonBody[GenericServerError].and(statusCode(StatusCode.InternalServerError)),
              GenericServerError("server failed"),
            )
          )
        }
        .options
    )
      .toRoutes(endpoints)
      .orNotFound
  }

}
