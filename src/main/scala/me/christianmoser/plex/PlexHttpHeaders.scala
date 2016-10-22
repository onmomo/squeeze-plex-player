package me.christianmoser.plex

import akka.http.scaladsl.model.headers.CustomHeader

case class PlexClientIdentifier(name: String = "X-Plex-Client-Identifier", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexProduct(name: String = "X-Plex-Product", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexVersion(name: String = "X-Plex-Version", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexDevice(name: String = "X-Plex-Device", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexDeviceName(name: String = "X-Plex-Device-Name", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexToken(name: String = "X-Plex-Token", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexPlatform(name: String = "X-Plex-Platform", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexPlatformVersion(name: String = "X-Plex-Platform-Version", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}

case class PlexProvides(name: String = "X-Plex-Provides", value: String) extends CustomHeader {
  override def renderInResponses(): Boolean = true

  override def renderInRequests(): Boolean = true
}
