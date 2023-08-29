package steve

import cats.MonadThrow
import cats.syntax.all._
import cats.MonadThrow

object ServerSideExecutor {

  def apply[F[_]](
    using F: Executor[F]
  ): Executor[F] = F

  def instance[F[_]: Interpreter: MonadThrow]: Executor[F] =
    new Executor[F] {
      private val emptyHash: Hash = Hash(List.empty)
      private val emptyState: SystemState = SystemState(Map.empty)

      private val resolveCommand: Build.Command => ResolvedBuild.Command = {
        case Build.Command.Upsert(key, value) => ResolvedBuild.Command.Upsert(key, value)
        case Build.Command.Delete(key)        => ResolvedBuild.Command.Delete(key)
      }
      private def resolve(build: Build): F[ResolvedBuild] = (build == Build.empty)
        .guard[Option]
        .as(emptyState)
        .liftTo[F](new Throwable("Unsupported build!"))
        .map(st => ResolvedBuild(st, build.commands.map(resolveCommand)))

      def build(build: Build): F[Hash] = resolve(build).flatMap(Interpreter[F].interpret).flatMap {
        case `emptyState` => emptyHash.pure
        case _            => new Throwable("Unsupported state").raiseError
      }

      def run(hash: Hash): F[SystemState] = (hash == emptyHash)
        .guard[Option]
        .as(SystemState(Map.empty))
        .liftTo[F](new Throwable("Unsupported hash!"))

    }

    def module[F[_]: MonadThrow]: Executor[F] = {
      given Interpreter[F] = Interpreter.instance[F]
      instance[F]
    }

}

trait Interpreter[F[_]] {
  def interpret(build: ResolvedBuild): F[SystemState]
}

object Interpreter {

  def apply[F[_]](
    using F: Interpreter[F]
  ): Interpreter[F] = F

  def instance[F[_]]: Interpreter[F] =
    new Interpreter {
      override def interpret(build: ResolvedBuild): F[SystemState] = ???
    }

}
