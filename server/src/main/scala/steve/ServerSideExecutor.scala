package steve

import cats.ApplicativeThrow
import cats.syntax.all._

object ServerSideExecutor {
  def apply[F[_]](using F: Executor[F]): Executor[F] = F

  def instance[F[_]: ApplicativeThrow]: Executor[F] =
    new Executor[F] {
      private val emptyHash: Hash = Hash(List.empty)

      def build(build: Build): F[Hash] = (build == Build.empty)
        .guard[Option]
        .as(emptyHash)
        .liftTo[F](new Throwable("Unsupported build!"))

      def run(hash: Hash): F[SystemState] = (hash == emptyHash)
        .guard[Option]
        .as(SystemState(Map.empty))
        .liftTo[F](new Throwable("Unsupported hash!"))

    }

}
