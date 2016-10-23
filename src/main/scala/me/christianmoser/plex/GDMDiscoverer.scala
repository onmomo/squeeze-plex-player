package me.christianmoser.plex

import java.io.{BufferedReader, ByteArrayInputStream, IOException, InputStreamReader}
import java.net._

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.Logging

import scala.concurrent.duration._


object GDMDiscoverer {
  def props(): Props = Props(new GDMDiscoverer())
}

sealed trait GDMDiscoveryMessage

case object GDMDiscovery extends GDMDiscoveryMessage

class GDMDiscoverer extends Actor with ActorLogging {

  private val broadcastAddress: String = "239.0.0.250"
  private val discoveryPort: Int = 32414
  private val discoveryMessage: String = "M-SEARCH * HTTP/1.1\r\n\r\n"

  def receive = {
    case GDMDiscovery =>
      log.debug("Starting GDM Discovery ...")

      try {
        val gdmAddress: InetAddress = InetAddress.getByName(broadcastAddress)
        val discoverySocket = new DatagramSocket
        val discoveryPacket: DatagramPacket = new DatagramPacket(discoveryMessage.getBytes, discoveryMessage.length, gdmAddress, discoveryPort)
        discoverySocket.send(discoveryPacket)
        val buf: Array[Byte] = new Array[Byte](4 * 1024)
        val responsePacket: DatagramPacket = new DatagramPacket(buf, buf.length)
        discoverySocket.receive(responsePacket)

        val serverAddress: String = responsePacket.getAddress.getHostAddress
        var serverPort: Int = 0
        var serverName: String = null
        val rdr: BufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(responsePacket.getData)))
        var line: String = rdr.readLine

        log.debug(line)
        if (line == "HTTP/1.0 200 OK") {
          log.debug(line)
          while (line != null) {
            line = rdr.readLine()
            log.debug(line)
            if (line != null) {
              val parts: Array[String] = line.split(":")
              if (parts.length == 2) {
                val name: String = parts(0)
                val value: String = parts(1).substring(1)
                if (name == "Port") {
                  serverPort = value.toInt
                }
                else if (name == "Name") {
                  serverName = value
                }
              }
            }
          }

          log.info("Found PLEX server '" + serverName + "' at " + serverAddress + ':' + serverPort)
          sender() ! PlexServer(serverName, serverAddress, serverPort)
        }

        discoverySocket.close()
      }
      catch {
        case e: IOException =>
          log.error(e, "Error during GDM Discovery.")
      }
  }

}
