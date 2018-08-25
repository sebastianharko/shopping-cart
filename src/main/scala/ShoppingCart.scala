import akka.actor.{ActorLogging, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration._

sealed trait Query

sealed trait Command

sealed trait Event

sealed trait Response

object ShoppingCart {

  case object GetCart extends Query

  case object Accepted extends Response

  case object Rejected extends Response

  case class AddItem(itemId: String, qty: Int) extends Command

  case class RemoveItem(itemId: String, qty: Int) extends Command

  case class ItemAdded(itemId: String, qty: Int) extends Event

  case class ItemRemoved(itemId: String, qty: Int) extends Event

  case object Stop

}

class ShoppingCart extends PersistentActor with ActorLogging {

  import ShoppingCart._

  var cart = Map[String, Int]()

  context.setReceiveTimeout(1 minutes)

  override def receiveCommand: Receive = {

    case GetCart ⇒
      sender() ! cart

    case AddItem(itemId: String, qty: Int) ⇒
      persist(ItemAdded(itemId, qty)) {
        e ⇒ onEvent(e)
          sender() ! Accepted
      }

    case RemoveItem(itemId: String, qty:Int) ⇒
      if (!cart.contains(itemId) || !(cart(itemId) >= qty)) {
        sender() ! Rejected
      } else {
        persist(ItemRemoved(itemId, qty)) {
          e ⇒
            onEvent(e)
            sender() ! Accepted
        }
      }

    case ReceiveTimeout ⇒
      context.parent ! Passivate(stopMessage = Stop)

    case Stop ⇒
      log.info("i'm going to sleep!")
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case e: Event ⇒
      log.info("replaying event {}", e)
      onEvent(e)
    case RecoveryCompleted ⇒
      log.info("recovery of actor with persistence id {} completed", persistenceId)
  }
  
  def onEvent(e: Event): Unit = {
    e match {
      case ItemAdded(itemId, qty) ⇒
        cart = cart + (itemId → (cart.getOrElse(itemId, 0) + qty))
      case ItemRemoved(itemId, qty) ⇒
        val numCurrentItems = cart(itemId)
        numCurrentItems - qty match {
          case 0 ⇒ cart = cart - itemId
          case _ ⇒ cart = cart - itemId + (itemId → (numCurrentItems - qty))
        }
    }
  }

  override def persistenceId: String = s"cart-${self.path.name}"


}
