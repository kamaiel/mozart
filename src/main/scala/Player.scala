package upmc.akka.leader

import scala.concurrent.ExecutionContext.Implicits.global
import MusicienActor._
import DataBaseActor._
import scala.concurrent.duration._
import akka.actor._


class Player extends Actor {
    device.open()
    def receive = {
        case Measure (l) => {
            l.foreach {
            case Chord (date,c) =>
                    c.foreach {
                    case Note(p,d,v) => self ! MidiNote(p,v,d,date)
                    }
            }  
        }     
        case MidiNote(p,v, d, at) => {
            context.system.scheduler.scheduleOnce ((at) milliseconds) (note_on (p,v,10))
            context.system.scheduler.scheduleOnce ((at+d) milliseconds) (note_off (p,10))
        }
        
    }
}