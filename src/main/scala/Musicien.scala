package upmc.akka.leader

import javax.sound.midi._
import javax.sound.midi.ShortMessage._
import scala.concurrent.duration._
import akka.actor._
import scala.collection.mutable.ArrayBuffer

object MusicienActor {
  case class MidiNote(pitch: Int, vel: Int, dur: Int, at: Int)
  val info =
    MidiSystem.getMidiDeviceInfo().filter(_.getName == "Gervill").headOption
  // or "SimpleSynth virtual input" or "Gervill"
  val device = info.map(MidiSystem.getMidiDevice).getOrElse {
    println("[ERROR] Could not find Gervill synthesizer.")
    sys.exit(1)
  }

  val rcvr = device.getReceiver()

  /////////////////////////////////////////////////
  def note_on(pitch: Int, vel: Int, chan: Int): Unit = {
    val msg = new ShortMessage
    msg.setMessage(NOTE_ON, chan, pitch, vel)
    rcvr.send(msg, -1)
  }

  def note_off(pitch: Int, chan: Int): Unit = {
    val msg = new ShortMessage
    msg.setMessage(NOTE_ON, chan, pitch, 0)
    rcvr.send(msg, -1)
  }
}

class Musicien(val id: Int, val terminaux: List[Terminal]) extends Actor {
  import DataBaseActor._
  val player = context.actorOf(Props(new Player))
  val db = context.actorSelection("/user/DataBase")
  val heart = context.actorOf(Props(new Heart(id)), "Heart")
  val orchestreJefe = context.actorOf(Props(new OrchestreJefe(id, heart, db)))
  var alivedMusicians = ArrayBuffer((-1, 0), (-1, 0), (-1, 0), (-1, 0))
  var displayActor = context.actorOf(Props[DisplayActor], name = "displayActor")
  alivedMusicians(id) = (0, alivedMusicians(id)._2)

  def receive = {
    case Start() => {
      heart ! Boumboum(terminaux, alivedMusicians)
    }

    case StartSymphony(alivedMusicians) => {
      displayActor ! Message("Id :" + id.toString + " Give Measure")
      orchestreJefe ! Play(terminaux, alivedMusicians)
    }

    case ExecuteSymphony(l) => {
      displayActor ! Message("Id : " + id.toString + " Play measure")
      player ! Measure(l)
    }

    case ShutDown() => {
      context.stop(self)
    }

    case UpdateMusicianStatus(id, status) => {
      alivedMusicians(id) = status
    }

  }
}
