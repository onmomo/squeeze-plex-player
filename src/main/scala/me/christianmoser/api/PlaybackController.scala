package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import grizzled.slf4j.Logging
import me.christianmoser.plex.{PlexServer, PlexServerApi}
import scala.concurrent.ExecutionContext.Implicits.global

class PlaybackController()(implicit system: ActorSystem) extends Logging {

  val plexServerApi = new PlexServerApi()

  def play(key: String, targetClientId: String, srvAddress: String, srvPort: Int, srvName: String, token: String) = {
    val plexServer = PlexServer(srvName, srvAddress, srvPort)
    plexServerApi.getTrack(key, plexServer, token) map { track =>
      logger.info(s"Resolved track: ${track.toString}")
    }
  }

}
