package me.christianmoser.api.model

case class Track(id: Int,
                 key: String,
                 relPath: String,
                 duration: Int,
                 thumb: String,
                 title: String,
                 location: String = "",
                 containerKey: String = "",
                 ratingKey: String = "",
                 state: String,
                 currentTime: Int = 0) {

  val itemType: String = "music"

  override def toString: String = {
    key + ": " + title
  }

}
