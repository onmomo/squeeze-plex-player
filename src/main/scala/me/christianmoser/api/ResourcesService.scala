package me.christianmoser.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.server.Directives._

trait ResourcesService extends Protocols with ScalaXmlSupport {

  implicit val system: ActorSystem

  def resourceRoutes = {

    val controller = new ResourceController()

//    logRequestResult("resources") {
      path("resources") {
        complete {
          controller.availableResources
        }
      }
//    }
  }
}
