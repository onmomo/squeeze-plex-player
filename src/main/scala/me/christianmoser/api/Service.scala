package me.christianmoser.api

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes.{Success, _}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import akka.http.scaladsl.model._
import ch.megard.akka.http.cors.CorsDirectives

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.xml.NodeSeq
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

/**
  * Created by chris on 05/06/16.
  */
trait Service extends Protocols with CorsDirectives with TimelineService with PlaybackService with ResourcesService {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter


  def plexResponseHeaders = {
    headers.Allow
  }

  val routes = cors() {
    resourceRoutes ~
      pathPrefix("player") {
        timelineRoutes ~
        playbackRoutes
      }
  }

}
