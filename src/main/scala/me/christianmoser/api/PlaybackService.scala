package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

trait PlaybackService extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter


  /**
    *
    * http://10-0-1-55.ffabbb0515d54c4b90cbca45fa0fee6d.plex.direct:32400/player/playback/playMedia
    * ?protocol=https
    * &key=%2Flibrary%2Fmetadata%2F14042
    * &address=10-0-1-55.ffabbb0515d54c4b90cbca45fa0fee6d.plex.direct
    * &port=32400
    * &token=transient-7da5f87d-7e7c-4d20-8b55-67ab4ddc6ad1
    * &containerKey=%2FplayQueues%2F53%3Fown%3D1%26window%3D200
    * &offset=13681
    * &machineIdentifier=8b09093707aeb5f359b26b88e0332263ecf47d56
    * &commandID=1
    *
    */



  def playbackRoutes = {

    val controller = new PlaybackController()

    logRequestResult("player-playback") {
      pathPrefix("playback") {
        parameters('commandID, 'address, 'port.as[Int], 'machineIdentifier, 'key, 'token) { (commandId, srvAddress, srvPort, srvName, key, token) =>
          headerValueByName("X-Plex-Target-Client-Identifier") { targetClientId =>
            path("playMedia") {
              logger.info(s"got: $srvAddress, $srvPort, $srvName, $key, $token, $targetClientId")
              controller.play(key, targetClientId, srvAddress, srvPort, srvName, token)
              complete {
                Success(200)("playMedia", "playMedia")
              }
            } ~
            path("seekTo") {
              complete {
                Success(200)("seekTo", "seekTo")
              }
            }
          }
        }
      }
    }
  }
}
