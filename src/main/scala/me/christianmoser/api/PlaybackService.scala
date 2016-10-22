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
