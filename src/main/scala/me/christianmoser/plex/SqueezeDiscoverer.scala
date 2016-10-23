package me.christianmoser.plex

import java.io.IOException

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.breeweb.slimconnect.data.Player
import com.breeweb.slimconnect.iface.delegate.Slimp3Delegate

object SqueezeDiscoverer {
  def props(lmsAddress: String, lmsPort: Int): Props = Props(new SqueezeDiscoverer(lmsAddress, lmsPort))
}

sealed trait SqueezeDiscoveryMessage

case object SqueezeDiscovery extends SqueezeDiscoveryMessage

case class SqueezePlayers(players: Map[String, Player]) extends SqueezeDiscoveryMessage

case class SqueezePlay(playerId: String, url: String) extends SqueezeDiscoveryMessage

class SqueezeDiscoverer(lmsAddress: String, lmsPort: Int) extends Actor {

  var players: Map[String, Player] = Map()

  val log = Logging(context.system, this)
  val interval = 10

  def receive = {
    case SqueezeDiscovery =>

      val slimp3Delegate = Slimp3Delegate.getInstance(lmsAddress, lmsPort)

      try {
        players = (slimp3Delegate.getPlayers() map (p => p.getId -> p) toMap)
        log.debug(s"Discovered ${players.size} Squeeze Devices.")
        slimp3Delegate.closeConnections()

        sender() ! SqueezePlayers(players)
      }
      catch {
        case e: IOException =>
          slimp3Delegate.closeConnections()
          log.error(e, "Error during Squeeze Discovery.")

      }
    case SqueezePlay(playerId, file) =>
      log.debug(s"Received Squeeze play advise for player $playerId and file $file")

      players.get(playerId) foreach  { player =>
        val playerInterface = Slimp3Delegate.getInstance(lmsAddress, lmsPort, player)
        try {
          playerInterface.playlistClear()
          playerInterface.playlistInsert(file)
          playerInterface.playlistPlay(file)
        } catch {
          case e: IOException =>
            playerInterface.closeConnections()
            log.error(e, "Error during Squeeze Playback.")

        }
      }

  }

}
