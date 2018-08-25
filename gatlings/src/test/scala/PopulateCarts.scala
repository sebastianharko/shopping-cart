import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object PopulateCarts {

  val item = List(
    "socks",
    "air freshener",
    "needle",
    "pool stick",
    "glasses",
    "flag",
    "clamp",
    "puddle",
    "sandal",
    "house",
    "nail file",
    "beef",
    "grid paper",
    "bag",
    "speakers",
    "book",
    "candle",
    "milk",
    "CD",
    "computer",
    "headphones",
    "paint brush",
    "tv",
    "blouse",
    "toilet",
    "toothpaste",
    "purse",
    "hair brush",
    "bowl",
    "thermometer",
    "thread",
    "boom box",
    "clay pot",
    "shampoo",
    "seat belt",
    "drawer",
    "lamp shade",
    "nail clippers",
    "magnet",
    "shoes",
    "deodorant",
    "car",
    "lotion",
    "shawl",
    "charger",
    "pencil",
    "eye liner",
    "ice cube tray",
    "outlet",
    "model car",
    "checkbook",
    "vase",
    "screw",
    "toe ring",
    "washing machine",
    "fork",
    "spring",
    "hair tie",
    "bow",
    "cat",
    "bananas",
    "teddies",
    "conditioner",
    "keyboard",
    "chapter book",
    "food",
    "mop",
    "canvas",
    "bottle",
    "shoe lace",
    "television",
    "apple",
    "key chain",
    "packing peanuts",
    "glass",
    "pillow",
    "spoon",
    "fake flowers",
    "twister",
    "tomato",
    "bookmark",
    "chocolate",
    "camera",
    "button",
    "fridge",
    "lip gloss",
    "pen",
    "desk",
    "picture frame",
    "brocolli",
    "street lights",
    "sharpie",
    "slipper",
    "greeting card",
    "phone",
    "face wash")

}

class PopulateCarts extends Simulation {

  import PopulateCarts._

  val feeder: Iterator[Map[String, Any]] = Iterator.continually {
    val itemId = item(Random.nextInt(item.size))
    val qty = Random.nextInt(100)
    val id = Random.nextInt(500)
    Map("itemId" → itemId, "qty" → qty, "id" → id)
  }

  val host = sys.env.getOrElse("APP_IP_PORT", "localhost:8080")
  val users = sys.env.get("USERS").map(_.toInt).getOrElse(100)
  val time = sys.env.get("PERIOD").map(_.toInt).getOrElse(2)


  val httpConf: HttpProtocolBuilder = http.shareConnections

  val scn1: ScenarioBuilder = scenario("One transaction").feed(feeder)
    .exec(
      http("transaction")
        .post(session ⇒ s"http://$host/cart/${session("id").as[String]}")
        .header("Content-Type", "application/json")
        .body(StringBody(session ⇒
          s"""
             |{
             |	"itemId": "${session("itemId").as[String]}",
             |	"qty": ${session("qty").as[Int]}
             |}
            """.stripMargin))
    )

  setUp(scn1.inject(constantUsersPerSec(users) during (time minutes))).protocols(httpConf)

}
