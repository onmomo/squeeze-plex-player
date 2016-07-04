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
trait Service extends Protocols with CorsDirectives with TimelineService with PlaybackService {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter


  def plexResponseHeaders = {
    headers.Allow

    //    response.setDate("Date", System.currentTimeMillis)
    //    response.setValue("Server", "Plex")
    //    response.setValue("Connection", "close")
    //    response.setContentType("application/xml")
    //    response.setValue("Accept-Ranges", "bytes")
    //    response.setValue("Access-Control-Allow-Origin", "*")
    //    response.setValue("Access-Control-Allow-Headers", "x-plex-version, x-plex-platform-version, x-plex-username, x-plex-client-identifier, x-plex-target-client-identifier, x-plex-device-name, x-plex-platform, x-plex-product, accept, x-plex-device")
    //    response.setValue("Access-Control-Allow-Methods", "POST, GET, OPTIONS, HEAD")
    //    response.setInteger("Access-Control-Max-Age", 1209600)
    //    response.setValue("X-Plex-Client-Identifier", clientId)
    //    response.setValue("Access-Control-Expose-Headers", "X-Plex-Client-Identifier")
  }

  def resourcesXml: NodeSeq = {
    <MediaContainer>
      <Player title="blubber" protocol="plex" protocolVersion="1" machineIdentifier="1234" protocolCapabilities="navigation,playback,timeline" deviceClass="sbt" product="blub"/>
    </MediaContainer>
  }

  val routes = cors() {
//    logRequestResult("akka-http-microservice") {
      path("resources") {
        complete {
          resourcesXml
        }
      } ~
      pathPrefix("player") {
        timelineRoutes ~
          playbackRoutes
      }
//    }
    //      pathPrefix("ip") {
    //        (get & path(Segment)) { ip =>
    //          complete {
    //            fetchIpInfo(ip).map[ToResponseMarshallable] {
    //              case Right(ipInfo) => ipInfo
    //              case Left(errorMessage) => BadRequest -> errorMessage
    //            }
    //          }
    //        } ~
    //        (post & entity(as[IpPairSummaryRequest])) { ipPairSummaryRequest =>
    //          complete {
    //            val ip1InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip1)
    //            val ip2InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip2)
    //            ip1InfoFuture.zip(ip2InfoFuture).map[ToResponseMarshallable] {
    //              case (Right(info1), Right(info2)) => IpPairSummary(info1, info2)
    //              case (Left(errorMessage), _) => BadRequest -> errorMessage
    //              case (_, Left(errorMessage)) => BadRequest -> errorMessage
    //            }
    //          }
    //        }
    //      }
    //    }
  }
}
