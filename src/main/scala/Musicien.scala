package upmc.akka.leader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._

case class Start ()

class Musicien (val id:Int, val terminaux:List[Terminal]) extends Actor {

     // Les differents acteurs du systeme
     val displayActor = context.actorOf(Props[DisplayActor], name = "displayActor")
    

     def receive = {

          // Initialisation
          case Start => {
               displayActor ! Message ("Musicien " + this.id + " is created")
                if (this.id == 0) {
                    println("Here")     
                    val remoteActor1 = context.actorSelection("akka.tcp://MozartSystem1@127.0.0.1:6001/user/Musicien1")
                    remoteActor1 ! Message("bon toutou")
                    context.system.scheduler.scheduleOnce(1800.millisecond, self, Start)
               }

          }

         case Message (content) => {
               println(content)
          }

     }
}
