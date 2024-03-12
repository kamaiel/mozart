package upmc.akka.leader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._

case class Message (content:String)

class DisplayActor extends Actor {

     def receive = {

          case Message (content) => {
               println(content)
               context.system.scheduler.scheduleOnce(1800.millisecond, self, Message("alive"))
          }

     }
}
