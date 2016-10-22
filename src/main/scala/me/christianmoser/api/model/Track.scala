package me.christianmoser.api.model

import scala.xml.NodeSeq

case class Track(id: Int,
                 key: String,
                 relPath: String,
                 duration: Int,
                 thumb: String,
                 title: String,
                 mediaContainer: NodeSeq,
                 location: String = "",
                 containerKey: String = "",
                 ratingKey: String = "",
                 state: String = "state?!",
                 currentTime: Int = 0) {

  val itemType: String = "music"

  override def toString: String = {
    key + ": " + title
  }

}
