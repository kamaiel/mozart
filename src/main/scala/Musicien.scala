package upmc.akka.leader

import javax.sound.midi._
import javax.sound.midi.ShortMessage._
import scala.concurrent.duration._
import akka.actor._


object MusicienActor {
  case class MidiNote (pitch:Int, vel:Int, dur:Int, at:Int) 
  val info = MidiSystem.getMidiDeviceInfo().filter(_.getName == "Gervill").headOption
  // or "SimpleSynth virtual input" or "Gervill"
  val device = info.map(MidiSystem.getMidiDevice).getOrElse {
     println("[ERROR] Could not find Gervill synthesizer.")
     sys.exit(1)
     }

     val rcvr = device.getReceiver()

     /////////////////////////////////////////////////
     def note_on (pitch:Int, vel:Int, chan:Int): Unit = {
     val msg = new ShortMessage
     msg.setMessage(NOTE_ON, chan, pitch, vel)
     rcvr.send(msg, -1)
     }

     def note_off (pitch:Int, chan:Int): Unit = {
     val msg = new ShortMessage
     msg.setMessage(NOTE_ON, chan, pitch, 0)
     rcvr.send(msg, -1)
     }
}

class Musicien (val id:Int, val terminaux:List[Terminal]) extends Actor {
     import DataBaseActor._
     val player = context.actorOf(Props(new Player))
     val db = context.actorSelection("/user/DataBase")
     val orchestreJefe = context.actorOf(Props(new OrchestreJefe(player,db)))
     val heart = context.actorOf(Props(new Heart(id)), "Heart")
     val displayActor = context.actorOf(Props[DisplayActor],"displayActor")
     var alivedMusicians = List((-1,0),(-1,0),(-1,0),(-1,0))
     alivedMusicians(id)_1 = 0

     def receive = {
           case Start() => {
               heart ! Boumboum(terminaux,alivedMusicians)
           }

           case ImTheLeader(id, timestamp) => {
               displayActor ! Message("It's work")
               // orchestreJefe ! Play()
           } 
     }          

}
     


