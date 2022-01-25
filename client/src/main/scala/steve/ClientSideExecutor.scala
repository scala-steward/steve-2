package steve

object ClientSideExecutor {

  def instance[F[_]]: Executor[F] =
    new Executor {
      def build(build: Build): F[Hash] = ???
      def run(run: Hash): F[SystemState] = ???
    }

}
