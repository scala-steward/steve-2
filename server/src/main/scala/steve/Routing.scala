package steve

import org.http4s.HttpApp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.ServerEndpoint
import cats.effect.kernel.Async

object Routing {

  def instance[F[_]: Async](exec: Executor[F]): HttpApp[F] = {

    val se: List[ServerEndpoint[Any, F]] = List(
      protocol.build.serverLogicSuccess(exec.build),
      protocol.run.serverLogicSuccess(exec.run),
    )
    Http4sServerInterpreter[F]().toRoutes(se).orNotFound
  }

}
