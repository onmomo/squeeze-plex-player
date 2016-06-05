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

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by chris on 05/06/16.
  */
trait Service extends Protocols with TimelineService with PlaybackService {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  lazy val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.ip-api.host"), config.getInt("services.ip-api.port"))

  val routes = {
    logRequestResult("akka-http-microservice") {
      pathPrefix("player") {
        timelineRoutes ~
        playbackRoutes
      }
    }
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
