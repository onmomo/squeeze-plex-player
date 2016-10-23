package me.christianmoser

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import me.christianmoser.api.Service
import me.christianmoser.plex._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.concurrent.duration._



object SqueezePlexHttpService extends App with Service {
  override implicit val system = ActorSystem("squeeze-plex-system")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  val squeezeDiscoverer = system.actorOf(SqueezeDiscoverer.props(lmsAddress = config.getString("squeezeplex.lms-address"), lmsPort = config.getInt("squeezeplex.lms-cli-port")), name = "SqueezeDiscoverer")
  // TODO announcement for each found squeeze device
  val gdmAnnouncer = system.actorOf(GDMAnnouncer.props(squeezeDiscoverer), name = "GmdAnnouncer")

  val gdmDiscoverer = system.actorOf(GDMDiscoverer.props(), name = "GdmDiscoverer")
  implicit val timeout = Timeout(5 seconds)
  (gdmDiscoverer ? GDMDiscovery).mapTo[PlexServer] map { plexServer =>
    logger.info("plex server discovered " + plexServer.name)
    (system.actorOf(PlexAuthenticator.props(squeezeDiscoverer)) ? PlexLogin).mapTo[PlexAuthenticated] map { plexAuthenticated =>
        logger.info("Successfully logged in to plex: " + plexAuthenticated.token)
    }

  }

}
