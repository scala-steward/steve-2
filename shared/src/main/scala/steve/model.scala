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

}

final case class Hash(value: List[Byte]) derives Codec.AsObject, Schema

final case class SystemState(getAll: Map[String, String]) derives Codec.AsObject, Schema

final case class GenericServerError(message: String) extends Exception(message)
  derives Codec.AsObject,
    Schema
