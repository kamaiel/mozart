package upmc.akka.leader

import MusicienActor._
import DataBaseActor._
import scala.concurrent.duration._
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global

class Heart(my_id:Int) extends Actor {
    val displayActor = context.actorOf(Props[DisplayActor], name = "displayActor")
    var leader_existant = false

    def receive = {
        case Boumboum(terminaux,alivedMusicians) => {
            alivedMusicians.forEach {
                case (alive, nb_without_news) => {
                    if (nb_without_news >= 2){
                        alivedMusicians(id)._1 = -1
                    }
                    if (alive == 1) leader_existant = true 
                }
            }
            if (!leader_existant) {
                alivedMusicians(my_id)._1 = 1
            }
            terminaux.foreach {(musicien) => {
                if (my_id != musicien.id) {
                    val remoteActor = context.actorSelection("akka.tcp://MozartSystem" + musicien.id + "@" + musicien.ip.replace("\"","") + ":" + musicien.port + "/user/Musicien" + musicien.id + "/Heart")
                    if (alivedMusicians(my_id)._1 == 1) {
                        remoteActor ! ImTheLeader(my_id,my_timestamp)
                    }else {
                        remoteActor ! ImAPlayer(my_id,my_timestamp)
                    }
                }
            }
            context.system.scheduler.scheduleOnce(800.millisecond, self, Boumboum(terminaux,alivedMusicians))
        }

        case ImAPlayer(id,timestamp) => {
            if(musicians_timestamp(id) == -1) musicians_timestamp(id) = timestamp
            println("I'm a player")
            context.parent ! ImTheLeader(my_id, my_timestamp)
            displayActor ! Message("pong")
        }

        case ImTheLeader(id,timestamp) => {
            if(musicians_timestamp(id) == -1) musicians_timestamp(id) = timestamp
            jefe_timestamp = System.currentTimeMillis()

        }
    }
}