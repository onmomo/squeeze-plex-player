package me.christianmoser.plex

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import me.christianmoser.api.Protocols
import spray.json._

import scala.concurrent.Future


object PlexAuthenticator {
  def props(): Props = Props(new PlexAuthenticator())
}

sealed trait PlexAuthenticatorMessage

case class PlexAuthenticated(token: String) extends PlexAuthenticatorMessage

case class PlexLogin() extends PlexAuthenticatorMessage

class PlexAuthenticator extends Actor with Protocols {

  val log = Logging(context.system, this)

  implicit val system: ActorSystem = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  lazy val authApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("plex.tv", 443)

  def authApiRequest(request: HttpRequest): Future[HttpResponse] = {

    val plexHeaders = List(
      headers.Authorization(BasicHttpCredentials(config.getString("squeezeplex.user"), config.getString("squeezeplex.password"))),
      PlexClientIdentifier(value = "1234"),
      PlexProduct(value = config.getString("squeezeplex.app-name")),
      PlexVersion(value = config.getString("squeezeplex.app-version"))
    )

    Source.single(request.withHeaders(plexHeaders)).via(authApiConnectionFlow).runWith(Sink.head)
  }

  def receive = {
    case PlexLogin =>
      try {
        log.debug("Try to authenticate ...")
        authApiRequest(RequestBuilding.Post("/users/sign_in.json")) map { response =>
          response.status match {
            case _: StatusCodes.Success =>
              // TODO unmarshal response to json case class someday..
              Unmarshal(response.entity).to[JsObject] map { obj =>
                obj.fields.get("user").foreach(usr => usr.asJsObject.fields.get("authToken").foreach { token =>
                  log.info("Successfully authenticated to plex.")
                  sender() ! PlexAuthenticated(token = token.convertTo[String])
                })
              }
            case _ => throw new RuntimeException("Failed to login to plex.tv")
          }
        }
      } catch {
        case _: Exception => log.warning("Error during plex authentication.")
      }
  }

}
