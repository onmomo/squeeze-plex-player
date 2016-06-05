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
    logRequestResult("player-playback") {
      pathPrefix("playback") {
        path("playMedia") {
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
