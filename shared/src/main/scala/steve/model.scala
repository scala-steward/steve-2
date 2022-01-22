package steve

enum Command {
    case Build()
    case Run(hash: Hash)
}

final case class Build(
    base: Build.Base,
    commands: List[Build.Command]
)

object Build {

    val empty = Build(
        base = Base.EmptyImage,
        commands = Nil
    )

    enum Base {
        case EmptyImage
        case ImageReference(hash: Hash)
    }

    enum Command {
        case Upsert(key: String, value: String)
        case Delete(key: String)
    }

}

final case class Hash(value: Array[Byte])