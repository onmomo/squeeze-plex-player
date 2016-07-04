package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config
import me.christianmoser.api.model.{ClientId, TimelineSubscriber}
import me.christianmoser.plex.PlexServer
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

import scala.concurrent.ExecutionContextExecutor

trait TimelineService extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  def timelineRoutes = {
//    logRequestResult("player-timeline") {
      pathPrefix("timeline") {
        extractClientIP { clientIP =>
          optionalHeaderValueByName("X-Plex-Client-Identifier") { clientId =>
            parameters('commandID, 'port.as[Int].?) { (commandId, port) =>
              path("poll") {
                complete {
                  // TODO not sure if TimelineSubscriber requires the server object
                  val server = PlexServer("blub", "0.0.0.0", 7777)
                  val timeline = TimelineSubscriber(commandId, clientIP.toOption, port.getOrElse(0), server, clientId.map(ClientId))
                  timeline.generateTimelineContainer(timeline.generateEmptyTimeline("music"))
                }
              } ~
              logRequestResult("player-timeline") {
                path("subscribe") {
                  complete {
                    // TODO not sure if TimelineSubscriber requires the server object
                    val server = PlexServer("blub", "0.0.0.0", 7777)
                    val subscriber = TimelineSubscriber(commandId, clientIP.toOption, port.getOrElse(0), server, clientId.map(ClientId))
//                    subscriber.setHttpClient(client)
//                    subscribers.put(clientId, subscriber)
                    Success(200)("OK", "OK")
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
    }
  }
}
