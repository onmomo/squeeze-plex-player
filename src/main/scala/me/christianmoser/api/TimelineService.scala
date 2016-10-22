package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config
import me.christianmoser.api.model.{ClientId, TimelineSubscriber}
import me.christianmoser.plex.{PlexServer, TimelineSubscriberActor}
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import me.christianmoser.plex.TimelineSubscriberActor._
import me.christianmoser.plex.Subscribe
import me.christianmoser.plex.Unsubscribe

import scala.concurrent.ExecutionContextExecutor

trait TimelineService extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  lazy val timelineSubscribeActor = system.actorOf(TimelineSubscriberActor.props())

  def timelineRoutes = {
    //    logRequestResult("player-timeline") {
    pathPrefix("timeline") {
      //      logRequestResult("timeline-service") {
      extractClientIP { clientIP =>
        optionalHeaderValueByName("X-Plex-Client-Identifier") { clientIdOption =>
          parameters('commandID) { (commandId) =>
            path("poll") {
              complete {
                // TODO not sure if TimelineSubscriber requires the server object
                val server = PlexServer("blub", "0.0.0.0", 7777)
                val timeline = TimelineSubscriber(commandId, clientIP.toOption, 0, server, clientIdOption.map(ClientId))
                timeline.generateTimelineContainer(timeline.generateEmptyTimeline("music"))
              }
            } ~
            parameters('port.as[Int]) { port =>
              path("subscribe") {
                complete {
                  // TODO not sure if TimelineSubscriber requires the server object
                  val server = PlexServer("blub", "0.0.0.0", 7777)
                  val subscriber = TimelineSubscriber(commandId, clientIP.toOption, port, server, clientIdOption.map(ClientId))

                  clientIdOption foreach { clientId =>
                    timelineSubscribeActor ! Subscribe(ClientId(clientId), subscriber)
                  }

                  Success(200)("Ok", "Ok")
                }
              } ~
              path("unsubscribe") {
                complete {
                  clientIdOption foreach { clientId =>
                    timelineSubscribeActor ! Unsubscribe(ClientId(clientId))
                  }

                  Success(200)("Ok", "Ok")
                }
              }
            }
          }
        }
        //      }
      }
    }
  }
}
