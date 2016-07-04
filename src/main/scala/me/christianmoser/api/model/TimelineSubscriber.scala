package me.christianmoser.api.model


//import org.apache.http.HttpEntity
//import org.apache.http.StatusLine
//import org.apache.http.client.ClientProtocolException
//import org.apache.http.client.methods.CloseableHttpResponse
//import org.apache.http.client.methods.HttpPost
//import org.apache.http.entity.StringEntity
//import org.apache.http.impl.client.CloseableHttpClient
//import org.apache.http.util.EntityUtils
//import java.io.IOException
import java.net.InetAddress
import java.util.logging.Level
import java.util.logging.Logger

import me.christianmoser.plex.PlexServer

import scala.xml.{Node, NodeSeq}


case class TimelineSubscriber(val commandId: String, val address: Option[InetAddress], val port: Int, val server: PlexServer, clientId: Option[ClientId]) {

  //  def updateTimeline(playable: Nothing): Nothing = {
  //    if (playable.getType eq Video.`type`) {
  //      return updateTimeline(playable.asInstanceOf[Nothing], playable.getState)
  //    }
  //    else {
  //      return updateTimeline(playable.asInstanceOf[Nothing], playable.getState)
  //    }
  //  }
  //
  //  def updateTimeline(audio: Nothing, state: String): Nothing = {
  //    val audioElement: Nothing = generateTimeline(audio, state)
  //    val photoElement: Nothing = generateEmptyTimeline("photo")
  //    val videoElement: Nothing = generateEmptyTimeline("video")
  //    return updateTimeline(audioElement, photoElement, videoElement)
  //  }

  //  private def updateTimeline(audioElement: Nothing, photoElement: Nothing, videoElement: Nothing): Nothing = {
  //    val container: Nothing = generateTimelineContainer(audioElement, photoElement, videoElement)
  //    logger.finer("Posting to " + postUrl)
  //    val post: Nothing = new Nothing(postUrl)
  //    post.addHeader("Host", address + ":" + port)
  //    post.addHeader("Content-Range", "bytes 0-/-1")
  //    post.addHeader("X-Plex-Client-Identifier", clientId)
  //    post.addHeader("X-Plex-Device", "stb")
  //    post.addHeader("X-Plex-Device-Name", clientName)
  //    post.addHeader("X-Plex-Provides", "player")
  //    val xml: String = container.toXML
  //    logger.finer("Sending " + xml)
  //    post.setEntity(new Nothing(xml))
  //    val httpResponse: Nothing = client.execute(post)
  //    val entity: Nothing = httpResponse.getEntity
  //    var response: Nothing = null
  //    if (entity != null && entity.getContentType != null && entity.getContentType.getValue.equals("application/xml")) {
  //      response = new Nothing(()).build(entity.getContent).getRootElement
  //    }
  //    else {
  //      val statusLine: Nothing = httpResponse.getStatusLine
  //      response = new Nothing("Response")
  //      response.addAttribute(new Nothing("code", Integer.toString(statusLine.getStatusCode)))
  //      response.addAttribute(new Nothing("status", statusLine.getReasonPhrase))
  //      EntityUtils.consume(entity)
  //    }
  //    if (logger.isLoggable(Level.FINE)) {
  //      logger.fine("Response was " + response.toXML)
  //    }
  //    return response
  //  }

  def generateAudioTimeline(audio: Track, clientId: ClientId) = {
    generateTimeline(audio, audio.state, clientId)
  }

  def generateTimeline(audio: Track, state: String, clientId: ClientId) = {
    <Timeline>
      <address>{server.address}</address>
      <containerKey>{audio.containerKey}</containerKey>
      <controllable>playPause,stop,skipPrevious,skipNext,seekTo,repeat</controllable>
      <duration>{audio.duration}</duration>
      <key>{audio.key}</key>
      <location>{audio.location}</location>
      <machineIdentifier>{clientId.identifier}</machineIdentifier>
      <mute>0</mute>
      <port>{server.port}</port>
      <protocol>http</protocol>
      <ratingKey>{audio.ratingKey}</ratingKey>
      <repeat>0</repeat>
      <seekRange>s"0-$
        {audio.duration}
        "</seekRange>
      <shuffle>0</shuffle>
      <state>{state}</state>
      <time>{audio.currentTime}</time>
      <audio>{audio.itemType}</audio>
      <volume>100</volume>
    </Timeline>
    //    val timeline: Nothing = new Nothing("Timeline")
    //    timeline.addAttribute(new Nothing("address", server.getAddress))
    //    timeline.addAttribute(new Nothing("containerKey", audio.getContainerKey))
    //    timeline.addAttribute(new Nothing("controllable", "playPause,stop,skipPrevious,skipNext,seekTo,repeat"))
    //    timeline.addAttribute(new Nothing("duration", Integer.toString(audio.getDuration)))
    //    timeline.addAttribute(new Nothing("key", audio.getKey))
    //    timeline.addAttribute(new Nothing("location", audio.getLocation))
    //    timeline.addAttribute(new Nothing("machineIdentifier", clientId.identifier))
    //    timeline.addAttribute(new Nothing("mute", "0"))
    //    timeline.addAttribute(new Nothing("port", Integer.toString(server.getPort)))
    //    timeline.addAttribute(new Nothing("protocol", "http"))
    //    timeline.addAttribute(new Nothing("ratingKey", audio.getRatingKey))
    //    timeline.addAttribute(new Nothing("repeat", "0"))
    //    timeline.addAttribute(new Nothing("seekRange", "0-" + Integer.toString(audio.getDuration)))
    //    timeline.addAttribute(new Nothing("shuffle", "0"))
    //    timeline.addAttribute(new Nothing("state", state))
    //    timeline.addAttribute(new Nothing("time", Integer.toString(audio.getCurrentTime)))
    //    timeline.addAttribute(new Nothing("type", audio.getType))
    //    timeline.addAttribute(new Nothing("volume", "100"))
    //    if (logger.isLoggable(Level.FINER)) {
    //      logger.finer(timeline.toXML)
    //    }
    //    return timeline
  }

  def generateEmptyTimeline(itemType: String): NodeSeq = {

    <Timeline>
      <location>navigation</location>
      <state>stopped</state>
      <time>0</time>
      <type>{itemType}</type>
    </Timeline>
    //    val timeline: Nothing = new Nothing("Timeline")
    //    timeline.addAttribute(new Nothing("location", "navigation"))
    //    timeline.addAttribute(new Nothing("state", "stopped"))
    //    timeline.addAttribute(new Nothing("time", "0"))
    //    timeline.addAttribute(new Nothing("type", `type`))
    //    return timeline
  }

  def generateTimelineContainer(musicTimeline: NodeSeq): NodeSeq = {

    <MediaContainer>
      <commandID>{commandId}</commandID>
      <location>fullScreenVideo</location>
      { musicTimeline }
    </MediaContainer>
    //    val container: Nothing = new Nothing("MediaContainer")
    //    container.addAttribute(new Nothing("commandID", commandId))
    //    var location: String = "fullScreenVideo"
    //    if (musicTimeline.getAttributeValue("location").equals("navigation") && videoTimeline.getAttributeValue("location").equals("navigation")) {
    //      location = "navigation"
    //    }
    //    container.addAttribute(new Nothing("location", location))
    //    container.appendChild(musicTimeline)
    //    container.appendChild(photoTimeline)
    //    container.appendChild(videoTimeline)
    //    return new Nothing(container)
  }
}

