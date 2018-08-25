import ShoppingCart._
import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s
import org.json4s.DefaultFormats

import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App with Json4sSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  implicit val timeout = akka.util.Timeout(5 seconds)
  implicit val serialization = json4s.jackson.Serialization // or native.Serialization
  implicit val formats = DefaultFormats

  val extractEntityId: ExtractEntityId = {
    case (id: String, payload: Command) ⇒ (id, payload)
    case (id: String, payload: Query) ⇒ (id, payload)
  }

  val numberOfShards = 100
  val extractShardId: ExtractShardId = {
    case (id: String, _: Command) ⇒
      (id.hashCode % numberOfShards).toString
    case (id: String, _: Query) ⇒
      (id.hashCode % numberOfShards).toString
  }

  val shoppingCarts = ClusterSharding(system).start(
    typeName = "ShoppingCart",
    entityProps = Props[ShoppingCart],
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId)

  val route =
    get {
      path("cart" / Segment) { id: String ⇒
        complete {
          (shoppingCarts ? (id, ShoppingCart.GetCart)).mapTo[Map[String, Int]]
        }
      }
    } ~ post {
      path("cart" / Segment) { id: String ⇒
        entity(as[AddItem]) { addItem: ShoppingCart.AddItem ⇒ {
          complete {
            (shoppingCarts ? (id, addItem)).mapTo[Response].map {
              case ShoppingCart.Accepted ⇒ StatusCodes.OK
              case ShoppingCart.Rejected ⇒ StatusCodes.BadRequest
            }
          }
        }
        }
      }
    } ~ delete {
      path("cart" / Segment) { id: String ⇒
        entity(as[RemoveItem]) { removeItem: ShoppingCart.RemoveItem ⇒ {
          complete {
            (shoppingCarts ? (id, removeItem)).mapTo[Response].map {
              case ShoppingCart.Accepted ⇒ StatusCodes.OK
              case ShoppingCart.Rejected ⇒ StatusCodes.BadRequest
            }
          }
        }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
}
