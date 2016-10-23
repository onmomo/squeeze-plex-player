package me.christianmoser.plex

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net._
import java.util.concurrent.{ScheduledFuture, TimeUnit}
import java.util.logging.Logger

import akka.event.Logging
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._


object GDMAnnouncer {
  def props(squeezeDiscoverer: ActorRef): Props = Props(new GDMAnnouncer(squeezeDiscoverer))
}

sealed trait GDMAnnouncementMessage

case object GDMAnnouncement extends GDMAnnouncementMessage

case class GDMPlayerAnnouncement(playerId: String, playerName: String) extends GDMAnnouncementMessage

class GDMAnnouncer(squeezeDiscoverer: ActorRef) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load()

  val squeezePlexAddress: String = "0.0.0.0"
  val squeezePlexPort: Int = 7777
  val announcePort: Int = 32412
  val interval = 10
  var announceSocket: MulticastSocket = null


  override def preStart() {

    log.debug("Scheduling a GDMAnnouncer to go out every " + interval + " seconds")

    val gdmAddress = InetAddress.getByName("239.0.0.250")
    val socketAddress = new InetSocketAddress(squeezePlexAddress, announcePort)
    announceSocket = new MulticastSocket(socketAddress)
    announceSocket.setSoTimeout(5000)
    announceSocket.setBroadcast(true)
    announceSocket.joinGroup(gdmAddress)

    context.system.scheduler.schedule(5 seconds, interval seconds, self, GDMAnnouncement)
  }

  override def postStop() = {
    announceSocket.close
  }

  def receive = {
    case GDMAnnouncement =>
      try {
        implicit val timeout = Timeout(2 seconds)
        (squeezeDiscoverer ? SqueezeDiscovery).mapTo[SqueezePlayers] map { squeezePlayers =>
          squeezePlayers.players foreach { player =>
            self ! GDMPlayerAnnouncement(playerId = player._1, playerName = player._2.getName)
          }
        }
      } catch {
        case ex: Exception =>
          log.error(ex, "Error during GDMAnnouncement.")
      }
    case GDMPlayerAnnouncement(playerId, playerName) =>
      try {
        val buf: Array[Byte] = new Array[Byte](4096)
        val pollPacket: DatagramPacket = new DatagramPacket(buf, buf.length)
        announceSocket.receive(pollPacket)
        val reader: BufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pollPacket.getData)))

        val packetContent = Stream.continually(reader.readLine()).takeWhile(_ != null)
        if (packetContent.contains("M-SEARCH * HTTP/1.1")) {
          log.debug(s"Send GDMAnnouncement packet for player $playerId / $playerName")
          val announcementMessage = announceMessage(playerId, playerName)
          val announcePacket: DatagramPacket = new DatagramPacket(announcementMessage.getBytes, announcementMessage.length, pollPacket.getAddress, pollPacket.getPort)
          announceSocket.send(announcePacket)
        }

        reader.close()
      } catch {
        case st: SocketTimeoutException =>
          log.debug("GDMAnnouncement timeout, try again.")
        case ex: Exception =>
          log.error(ex, "Error during GDMPlayerAnnouncement.")
      }

  }

  private def announceMessage(clientId: String, name: String) = {
    implicit val sb: StringBuilder = new StringBuilder("HTTP/1.0 200 OK\r\n")
    appendParameter("Content-Type", "plex/media-player")
    appendParameter("Resource-Identifier", clientId)
    appendParameter("Device-Class", "sbt")
    appendParameter("Name", name)
    appendParameter("Port", squeezePlexPort.toString)
    appendParameter("Product", config.getString("squeezeplex.app-name"))
    appendParameter("Protocol", "plex")
    appendParameter("Protocol-Capabilities", "timeline,playback")
    appendParameter("Protocol-Version", "1")
    appendParameter("Version", "0.0.1")

    val announceMessage = sb.toString
//    log.debug(announceMessage)

    announceMessage
  }

  private def appendParameter(name: String, value: String)(implicit sb: StringBuilder) = {
    sb.append(name + ": " + value + "\r\n")
  }

}
