package upmc.akka.leader

import MusicienActor._
import DataBaseActor._
import scala.concurrent.duration._
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import upmc.akka.leader.Projet.{leader}


class Heart(my_id:Int, my_ip:String, my_port: Int) extends Actor {
    case object PreElect
    case class PreElectResponse(status: Int)

    val displayActor = context.actorSelection("akka.tcp://MozartSystem" + my_id + "@" + my_ip.replace("\"","") + ":" + my_port + "/user/Musicien" + my_id + "/displayActor")
    var waiting_turn_for_player = 0 
    var election_jeffe = 0 
    var leader_existant = false
    var last_leader = -1
    var finished_my_instruction = true

    def receive = {
        case Boumboum(terminaux,alivedMusicians) => {
            for ((status_nb_without_news, idx) <- alivedMusicians.zipWithIndex) {
                val (status, nb_without_news) = status_nb_without_news

                if (nb_without_news >= 10 && idx != my_id) {
                    alivedMusicians(idx) = (-1, 0)
                }

                if (status == 1) leader_existant = true

                if (idx != my_id) {
                    val i = alivedMusicians(idx)._2 + 1
                    alivedMusicians(idx) = (alivedMusicians(idx)._1, i)
                }
            }
            if (!leader_existant) {
                election_jeffe += 1 
                if (election_jeffe >= 15 && last_leader == -1){
                    alivedMusicians.indexWhere(_._1 == 0) match {
                        case -1 => ()
                        case index => {
                            alivedMusicians(index) = (1, 0)
                            last_leader = index
                        }
                    }
                }else if (election_jeffe > 4 && last_leader != -1) {
                     alivedMusicians.indexWhere(_._1 == 0) match {
                        case -1 => ()
                        case index => {
                            alivedMusicians(index) = (1, 0)
                            last_leader = index
                        }
                    }
                }
            }else {
                election_jeffe = 0
            }
            
            if(alivedMusicians(my_id)._1 == 1) {
                alivedMusicians.indexWhere(_._1 == 0) match {
                    case -1 => {
                        displayActor ! Message ("Waiting for players ...")
                        waiting_turn_for_player +=1
                    }
                    case index => waiting_turn_for_player = 0 
                }
            }

            if (waiting_turn_for_player > 100) {
                context.parent ! ShutDown()
            }
            
            terminaux.foreach {(musicien) => {
                if (my_id != musicien.id) {
                    val remoteActor = context.actorSelection("akka.tcp://MozartSystem" + musicien.id + "@" + musicien.ip.replace("\"","") + ":" + musicien.port + "/user/Musicien" + musicien.id + "/Heart")
                    if (alivedMusicians(my_id)._1 == 1) {
                        remoteActor ! ImTheLeader(my_id, alivedMusicians)
                    }else {
                        remoteActor ! ImAPlayer(my_id,alivedMusicians)
                    }
                }
            }}
            leader_existant = false
            context.system.scheduler.scheduleOnce(300.millisecond, context.parent, Start())
        }

        case ImAPlayer(id,alivedMusicians) => {
            context.parent ! UpdateMusicianStatus(id, (0, 0))
            if (alivedMusicians(my_id)._1 == 1 && finished_my_instruction) {
                finished_my_instruction = false 
                context.parent ! StartSymphony(alivedMusicians)
            }
        }

        case ImTheLeader(id, alivedMusicians) => {
            election_jeffe = 0 
            last_leader = id 
            context.parent ! UpdateMusicianStatus(id, (1, 0))
        }

        case FinishInstruction() => {
            finished_my_instruction = true
        }
    }
}