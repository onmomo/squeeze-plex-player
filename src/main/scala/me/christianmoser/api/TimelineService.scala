package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

trait TimelineService extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  def timelineRoutes = {
    logRequestResult("player-timeline") {
      pathPrefix("timeline") {
        path("poll") {
          complete {
            Success(200)("poll", "poll")
          }
        } ~
        path("subscribe") {
          complete {
            Success(200)("subscribe", "subscribe")
          }
        } ~
        path("unsubscribe") {
          complete {
            Success(200)("unsubscribe", "unsubscribe")
          }
        }
      }
    }
  }
}
