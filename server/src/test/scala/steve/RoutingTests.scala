package steve

import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.dsl.io.*
import cats.syntax.all.*
import org.http4s.client.Client
import cats.effect.IO
import org.http4s.Method._
import org.http4s.Method._
import org.http4s.implicits.*
import io.circe.Json

class RoutingTests extends CatsEffectSuite {

  val client = Client.fromHttpApp[IO](
    Routing.instance(
      TestExecutor.instance(
        Map.empty,
        Map(
          Hash(List(40, 100)) -> SystemState(Map("K" -> "V")).asRight
        ),
      )
    )
  )

  test("POST /api/run") {
    val body =
      io.circe
        .parser
        .parse(
          """
          |{
          |  "value": [40, 100]
          |}
        """.stripMargin
        )
        .toOption
        .get

    val output =
      io.circe
        .parser
        .parse(
          """
          |{
          |  "all": {
          |    "K": "V"
          |  }
          |}
        """.stripMargin
        )
        .toOption
        .get

    assertIO(
      client.expect[Json](POST(body, uri"/api/run")),
      output,
    )
  }

}
