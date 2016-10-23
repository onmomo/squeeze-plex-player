package me.christianmoser.api

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.breeweb.slimconnect.data.Player
import grizzled.slf4j.Logging
import me.christianmoser.plex._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.xml.NodeSeq

class ResourceController()(implicit system: ActorSystem) extends Logging {

  def availableResources() = {
    implicit val timeout = Timeout(2 seconds)
    system.actorSelection("user/SqueezeDiscoverer").resolveOne() flatMap { squeezeDiscoverer =>
      (squeezeDiscoverer ? SqueezeDiscovery).mapTo[SqueezePlayers] map { squeezePlayers =>
        resourcesXml(squeezePlayers.players)
      }
    }
  }

  private def resourcesXml(players: Map[String, Player]): NodeSeq = {
    <MediaContainer>
      {players map { player =>
        <Player name={player._2.getName} protocol="plex" protocolVersion="1" machineIdentifier={player._1} protocolCapabilities="playback,timeline" deviceClass="sbt" />
    }}
    </MediaContainer>
  }

}
