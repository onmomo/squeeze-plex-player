package me.christianmoser

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import me.christianmoser.api.Service
import me.christianmoser.plex.{GDMAnnouncer, GDMDiscoverer, GDMDiscovery}


object SqueezePlexHttpService extends App with Service {
  override implicit val system = ActorSystem("squeeze-plex-system")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  // TODO announcement for each found squeeze device
  val gdmAnnouncer = system.actorOf(GDMAnnouncer.props(name = "squeeze-plex", clientId = "squeeze-device-1"))

  val gdmDiscoverer = system.actorOf(GDMDiscoverer.props())
  gdmDiscoverer -> GDMDiscovery
}
