package me.christianmoser.plex

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import me.christianmoser.api.Protocols
import spray.json._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._

import scala.concurrent.Future


object PlexAuthenticator {
  def props(squeezeDiscoverer: ActorRef): Props = Props(new PlexAuthenticator(squeezeDiscoverer))
}

sealed trait PlexAuthenticatorMessage

case class PlexAuthenticated(token: String) extends PlexAuthenticatorMessage

case class PlexLogin() extends PlexAuthenticatorMessage

case class PlexLoginPlayer(playerId: String, playerName: String) extends PlexAuthenticatorMessage

class PlexAuthenticator(squeezeDiscoverer: ActorRef) extends Actor with Protocols {

  val log = Logging(context.system, this)

  implicit val system: ActorSystem = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  lazy val authApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("plex.tv", 443)

  def authApiRequest(request: HttpRequest, playerId: String, playerName: String): Future[HttpResponse] = {

    val plexHeaders = List(
      headers.Authorization(BasicHttpCredentials(config.getString("squeezeplex.user"), config.getString("squeezeplex.password"))),
      PlexClientIdentifier(value = playerId),
      PlexDeviceName(value = playerName),
      PlexProduct(value = config.getString("squeezeplex.app-name")),
      PlexProvides(value = "player,client"),
      PlexPlatform(value = "Konvergo"),
      PlexDevice(value = "Logitech Squeezebox"),
      PlexVersion(value = config.getString("squeezeplex.app-version"))
    )

    Source.single(request.withHeaders(plexHeaders)).via(authApiConnectionFlow).runWith(Sink.head)
  }

  def receive = {
    case PlexLogin =>
      try {
        implicit val timeout = Timeout(2 seconds)
        (squeezeDiscoverer ? SqueezeDiscovery).mapTo[SqueezePlayers] map { squeezePlayers =>
          squeezePlayers.players foreach { player =>
            self ! PlexLoginPlayer(playerId = player._1, playerName = player._2.getName)
          }
        }
      } catch {
        case _: Exception => log.warning("Error during plex authentication device lookup.")
      }
    case PlexLoginPlayer(playerId, playerName) =>
      try {
        log.debug(s"Try to authenticate player $playerId / $playerName")

        authApiRequest(RequestBuilding.Post("/users/sign_in.json"), playerId, playerName) map { response =>
          response.status match {
            case _: StatusCodes.Success =>
              // TODO unmarshal response to json case class someday..
              Unmarshal(response.entity).to[JsObject] map { obj =>
                obj.fields.get("user").foreach(usr => usr.asJsObject.fields.get("authToken").foreach { token =>
                  log.info(s"Successfully authenticated player ${playerId} to plex.")
                  sender() ! PlexAuthenticated(token = token.convertTo[String])
                })
              }
            case _ =>
              log.warning(s"Failed to login player ${playerId} to plex.tv. Check credentials!")
          }
        }
      } catch {
        case _: Exception => log.warning(s"Error during plex authentication for player $playerId.")
      }

  }

}
