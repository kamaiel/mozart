package upmc.akka.leader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._
import scala.collection.mutable.ArrayBuffer

case class Message (content:String)
case class DisplayTab (tab:ArrayBuffer[(Int,Int)])

class DisplayActor extends Actor {

     def receive = {
          case Message (content) => {
               println(content)
          }

          case DisplayTab (tab) => {
               var to_print = ""
               for (((status,_), idx) <- tab.zipWithIndex) {
                    to_print += "|" + status 
               }
               println(to_print + "|")
          }
     }
}
