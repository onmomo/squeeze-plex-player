package me.christianmoser.api.model

case class Track(location: String,
                 containerKey: String,
                 key: String,
                 ratingKey: String,
                 title: String,
                 file: String,
                 duration: Int,
                 state: String,
                 currentTime: Int) extends Playable {

  val itemType: String = "music"

  override def toString: String = {
    file + ": " + title
  }

}
