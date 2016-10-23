package me.christianmoser.api

import akka.actor.{ActorRef, ActorSystem}
import grizzled.slf4j.Logging
import me.christianmoser.plex.{PlexServer, PlexServerApi, SqueezePlay}

import scala.concurrent.ExecutionContext.Implicits.global

class PlaybackController()(implicit system: ActorSystem) extends Logging {

  val plexServerApi = new PlexServerApi()

  def play(key: String, targetClientId: String, srvAddress: String, srvPort: Int, srvName: String, token: String) = {
    val plexServer = PlexServer(srvName, srvAddress, srvPort)
    plexServerApi.getTrack(key, plexServer, token) map { track =>
      // TODO refactor
      val url = plexServer.getPrefix() + track.key + "?X-Plex-Token=" + token
      system.actorSelection("user/SqueezeDiscoverer").tell(SqueezePlay(targetClientId, url), ActorRef.noSender)

      track.mediaContainer
    }
  }

}
