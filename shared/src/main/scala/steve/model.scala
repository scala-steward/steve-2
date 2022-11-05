package steve

import io.circe.Codec
import sttp.tapir.Schema

enum Command derives Codec.AsObject, Schema {
  case Build()
  case Run(hash: Hash)
}

final case class Build(
  base: Build.Base,
  commands: List[Build.Command],
) derives Codec.AsObject,
    Schema

object Build {

  def compile(build: Build): List[]

  def execute(build: Build, initState: SystemState, builds: (Hash, SystemState) => SystemState): SystemState = {
    val initState1 = build.base match {
      case Base.EmptyImage => initState
      case Base.ImageReference(hash) => builds(hash, initState)
    }
    build.commands.foldLeft(initState1) {
      case (state, command) =>
        applyCommand(command, state)
    }
  }

  def applyCommand(command: Command, systemState: SystemState): SystemState = {
    command match {
      case Command.Upsert(key, value) => SystemState(systemState.all.updated(key, value))
      case Command.Delete(key) => SystemState(systemState.all - key)
    }
  }
  
  val empty = Build(
    base = Base.EmptyImage,
    commands = Nil,
  )

  enum Base derives Codec.AsObject, Schema {
    case EmptyImage
    case ImageReference(hash: Hash)
  }

  enum Command derives Codec.AsObject, Schema {
    case Upsert(key: String, value: String)
    case Delete(key: String)
  }


  enum Error extends Exception derives Codec.AsObject, Schema {
    case UnknownBase(hash: Hash)
  }

}

final case class Hash(value: List[Byte]) derives Codec.AsObject, Schema

final case class SystemState(all: Map[String, String]) derives Codec.AsObject, Schema

final case class GenericServerError(message: String) extends Exception(message)
  derives Codec.AsObject,
    Schema
