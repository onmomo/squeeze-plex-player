package me.christianmoser.plex

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net._
import java.util.concurrent.{ScheduledFuture, TimeUnit}
import java.util.logging.Logger

import akka.event.Logging
import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


object GDMAnnouncer {
  def props(name: String, clientId: String): Props = Props(new GDMAnnouncer(name, clientId))
}

sealed trait GDMAnnouncementMessage

case object GDMAnnouncement extends GDMAnnouncementMessage

class GDMAnnouncer(name: String = "Squeeze-Plex", clientId: String, playerAddress: String = "0.0.0.0", svrPort: Int = 7777) extends Actor {
  val log = Logging(context.system, this)

  val sb: StringBuilder = new StringBuilder("HTTP/1.0 200 OK\r\n")
  sb.append("Content-Type: plex/media-player\r\n")
  sb.append("Resource-Identifier: " + clientId + "\r\n")
  sb.append("Device-Class: stb\r\n")
  sb.append("Name: " + name + "\r\n")
  sb.append("Port: " + svrPort + "\r\n")
  sb.append("Product: " + "Squeeze-Plex" + "\r\n")
  sb.append("Protocol: plex\r\n")
  sb.append("Protocol-Capabilities: playback,timeline\r\n")
  sb.append("Protocol-Version: 1\r\n")
  sb.append("Version: 0.0.1\r\n")
  sb.append("\r\n")
  var announceMessage = sb.toString
  log.debug(announceMessage)

  val announcePort: Int = 32412
  val interval = 10
  var announceSocket: MulticastSocket = null

  override def preStart() {

    log.debug("Scheduling a GDMAnnouncer to go out every " + interval + " seconds")

    val gdmAddress = InetAddress.getByName("239.0.0.250")
    val socketAddress = new InetSocketAddress(playerAddress, announcePort)
    announceSocket = new MulticastSocket(socketAddress)
    announceSocket.setSoTimeout(5000)
    announceSocket.setBroadcast(true)
    announceSocket.joinGroup(gdmAddress)

    context.system.scheduler.schedule(1 seconds, interval seconds, self, GDMAnnouncement)
  }

  override def postStop() = {
    announceSocket.close
  }

  def receive = {
    case GDMAnnouncement =>
      log.debug("GDMAnnouncement ...")
      try {
        val buf: Array[Byte] = new Array[Byte](1000)
        val pollPacket: DatagramPacket = new DatagramPacket(buf, buf.length)
        announceSocket.receive(pollPacket)
        val reader: BufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pollPacket.getData)))

        val packetContent = Stream.continually(reader.readLine()).takeWhile(_ != null)
         if (packetContent.contains("M-SEARCH * HTTP/1.1")) {
           log.debug("Send GDMAnnouncement packet")
           val announcePacket: DatagramPacket = new DatagramPacket(announceMessage.getBytes, announceMessage.length, pollPacket.getAddress, pollPacket.getPort)
           announceSocket.send(announcePacket)
         }
        reader.close()
      } catch {
        case st: SocketTimeoutException =>
          log.debug("GDMAnnouncement timeout, try again.")
        case ex: Exception =>
          log.error(ex, "Error during GDMAnnouncing.")
      }
  }
}
