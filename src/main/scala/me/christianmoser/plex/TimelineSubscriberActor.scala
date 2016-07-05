package me.christianmoser.plex


import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers.ByteRange
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import me.christianmoser.api.model.{ClientId, TimelineSubscriber, Track}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.xml.{Elem, NodeSeq}


object TimelineSubscriberActor {
  def props(): Props = Props(new TimelineSubscriberActor())
}

sealed trait TimelineSubscriberMessage

case class Subscribe(clientId: ClientId, timelineSubscriber: TimelineSubscriber) extends TimelineSubscriberMessage
case class Unsubscribe(clientId: ClientId) extends TimelineSubscriberMessage
case class Update(track: Track) extends TimelineSubscriberMessage

class TimelineSubscriberActor extends Actor {

  val log = Logging(context.system, this)

  val config = ConfigFactory.load()

  private var subscribers: Map[ClientId, TimelineSubscriber] = Map()

  implicit val system: ActorSystem = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def updateSubscribersConnectionFlow(postUrl: String): Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(postUrl)

  def updateSubscriptions(request: HttpRequest, timelineSubscriber: TimelineSubscriber, updateElem: Elem): Future[HttpResponse] = {
//    post.addHeader("Content-Range", "bytes 0-/-1")

    val subscriberAddress = timelineSubscriber.address.map(_.getHostAddress + ":" + timelineSubscriber.port).getOrElse("no-address")
    val postUrl = subscriberAddress + "/:/timeline"

    val plexHeaders = List(
      headers.Host(subscriberAddress),
      headers.Range.apply(ByteRange(0, 1)),
      PlexClientIdentifier(value = timelineSubscriber.clientId.map(_.identifier).getOrElse("")),
      PlexDevice(value = "sbt"),
      PlexProvides(value = "player"),
      PlexDeviceName(value = config.getString("squeezeplex.app-name"))
    )

    Source.single(request.withHeaders(plexHeaders)).via(updateSubscribersConnectionFlow(postUrl)).runWith(Sink.head)
  }



  def receive = {
    case Subscribe(clientId, timeline) =>
      log.debug("Subscribing timeline for client {}", clientId)
      subscribers += clientId -> timeline
    case Unsubscribe(clientId) =>
      log.debug("Usubscribing timeline for client {}", clientId)
      subscribers -= clientId
    case Update(track) =>
      subscribers foreach { subscriber =>
        val elem = subscriber._2.generateAudioTimeline(track, subscriber._1)
        updateSubscriptions(RequestBuilding.Post(), subscriber._2, elem) map { response =>
        log.debug("Received response for timeline update, client {}: {}", subscriber._1.identifier)
        }
      }
  }

}
