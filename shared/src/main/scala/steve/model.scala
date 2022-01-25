package steve

import io.circe.Codec

enum Command {
    case Build()
    case Run(hash: Hash)
}

final case class Build(
    base: Build.Base,
    commands: List[Build.Command]
) derives Codec.AsObject

object Build {

    val empty = Build(
        base = Base.EmptyImage,
        commands = Nil
    )

    enum Base derives Codec.AsObject {
        case EmptyImage
        case ImageReference(hash: Hash)
    }

    enum Command derives Codec.AsObject {
        case Upsert(key: String, value: String)
        case Delete(key: String)
    }

}

final case class Hash(value: Array[Byte]) derives Codec.AsObject

final case class SystemState(getAll: Map[String, String]) derives Codec.AsObject
