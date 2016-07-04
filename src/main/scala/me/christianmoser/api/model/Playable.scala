package me.christianmoser.api.model

abstract class Playable {
  val itemType: String
  val location: String
  val containerKey: String
  val key: String
  val ratingKey: String
  val title: String
  val file: String
  val duration: Int
  val state: String
  val currentTime: Int
}