package me.christianmoser.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

trait PlaybackService extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  def playbackRoutes = {
    logRequestResult("player-playback") {
      pathPrefix("playback") {
        path("playMedia") {
//          Request : HttpRequest(HttpMethod(GET),http://10-0-1-115.2ca4e7bee8ec4aa7802ede153194a5cb.plex.direct:32400/player/playback/playMedia?protocol=https&key=%2Flibrary%2Fmetadata%2F23468&address=10-0-1-123.2ca4e7bee8ec4aa7802ede153194a5cb.plex.direct&port=32400&token=transient-fa15c22c-ec19-4f5c-b531-e881d4e70d9b&containerKey=%2FplayQueues%2F12%3Fown%3D1%26window%3D200&offset=0&machineIdentifier=eead6cdaf60ba3e2223a4a6fd5bf4fd897bb99c9&commandID=1,List(Remote-Address: 10.0.3.124:49743, Origin: https://app.plex.tv, X-Plex-Token: AA8Q4BBV3sC1mTq9JZXQ, Accept-Language: en, X-Plex-Platform: Chrome, Accept-Encoding: gzip, deflate, sdch, br, X-Plex-Client-Identifier: aa111e49-d172-44d7-9286-89e086e8e3db, Connection: close, Accept: text/plain, */*;q=0.01, User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36, X-Plex-Device: OSX, X-Plex-Device-Screen-Resolution: 1184x683,1440x900, X-Plex-Product: Plex Web, Referer: https://app.plex.tv/web/app, X-Plex-Platform-Version: 51.0, Host: 0.0.0.0.2ca4e7bee8ec4aa7802ede153194a5cb.plex.direct:32400, X-Plex-Device-Name: Plex Web (Chrome), X-Plex-Version: 2.7.2, X-Plex-Target-Client-Identifier: squeeze-device-1, Timeout-Access: <function1>),HttpEntity.Strict(none/none,ByteString()),HttpProtocol(HTTP/1.1))
//            Response: Complete(HttpResponse(200 playMedia,List(),HttpEntity.Strict(text/plain; charset=UTF-8,playMedia),HttpProtocol(HTTP/1.1)))

            complete {
            Success(200)("playMedia", "playMedia")
          }
        } ~
        path("seekTo") {
          complete {
            Success(200)("seekTo", "seekTo")
          }
        }
      }
    }
  }
}
