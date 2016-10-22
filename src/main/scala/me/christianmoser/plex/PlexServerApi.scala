package me.christianmoser.plex

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import grizzled.slf4j.Logging
import me.christianmoser.api.model.Track

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class PlexServerApi()(implicit system: ActorSystem) extends ScalaXmlSupport with Logging {

  implicit lazy val materializer: Materializer = ActorMaterializer()

  def getTrack(key: String, server: PlexServer, authToken: String): Future[Track] = {

    val plexHeaders = List(
      PlexToken(value = authToken)
    )

    serverApiRequest(RequestBuilding.Get(key), plexHeaders, server) flatMap { response =>
      response.status match {
        case _: StatusCodes.Success =>
          Unmarshal(response.entity).to[NodeSeq] flatMap { mediaContainer =>
            try {
//              logger.info(s"MediaContainer: ${mediaContainer}")
              val id = (mediaContainer \ "Track" \ "Media" \ "@id").text.toInt
              val file = (mediaContainer \ "Track" \ "Media" \ "Part" \ "@key").text
              val relPath = (mediaContainer \ "Track" \ "Media" \ "Part" \ "@file").text
              val duration = (mediaContainer \ "Track" \ "Media" \ "Part" \ "@duration").text.toInt
              val thumb = (mediaContainer \ "Track" \ "@thumb").text
              val title = (mediaContainer \ "Track" \ "@title").text

              Future.successful(Track(id, file, relPath, duration, thumb, title, mediaContainer))
            } catch {
              case e: Exception =>
                logger.warn("Unable to parse mediaContainer response", e)
                Future.failed(e)
            }
          }
        case _ => Future.failed(new RuntimeException(s"Failed to resolve $key from plex server"))
      }
    }
  }

  private def serverConnectionFlow(srvAddress: String, srvPort: Int): Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps(srvAddress, srvPort)


  private def serverApiRequest(request: HttpRequest,
                               plexHeaders: List[HttpHeader],
                               server: PlexServer): Future[HttpResponse] = {
    Source.single(request.withHeaders(plexHeaders)).via(serverConnectionFlow(server.address, server.port)).runWith(Sink.head)
  }

  //
  //  		<MediaContainer size="1" allowSync="1" identifier="com.plexapp.plugins.library" librarySectionID="1" librarySectionTitle="Music" librarySectionUUID="4bd2150a-6d78-4c0c-b349-57630b19065d" mediaTagPrefix="/system/bundle/media/flags/" mediaTagVersion="1473718761">
  //  			<Track ratingKey="13643" key="/library/metadata/13643" parentRatingKey="13642" grandparentRatingKey="13641" guid="com.plexapp.agents.plexmusic://gracenote/track/265394571-20255FEE88859477BE1ACE5F783DBF92/265394572-FC308B9ADE5DF732A707B87288A80783?lang=de" librarySectionID="1" type="track" title="Bortgang" grandparentKey="/library/metadata/13641" parentKey="/library/metadata/13642" grandparentTitle="Solbrud" parentTitle="Solbrud" originalTitle="Solbrud" summary="" index="1" parentIndex="1" ratingCount="2912" viewCount="3" lastViewedAt="1477081995" thumb="/library/metadata/13642/thumb/1471585620" art="/library/metadata/13641/art/1476409445" parentThumb="/library/metadata/13642/thumb/1471585620" grandparentThumb="/library/metadata/13641/thumb/1476409445" grandparentArt="/library/metadata/13641/art/1476409445" duration="727613" addedAt="1471585545" updatedAt="1471585620" chapterSource="">
  //  				<Media id="12530" duration="727613" bitrate="1027" audioChannels="2" audioCodec="flac" container="flac">
  //  					<Part id="12530" key="/library/parts/12530/1471449540/file.flac" duration="727613" file="/home/kodi/nas/music/lossless/off-site/other/Solbrud - Solbrud/Solbrud - Solbrud - 01 Bortgang.flac" size="93408008" container="flac" hasThumbnail="1">
  //  						<Stream id="12962" streamType="2" selected="1" codec="flac" index="0" channels="2" bitrate="1026" audioChannelLayout="stereo" bitDepth="16" bitrateMode="vbr" duration="727613" samplingRate="44100" />
  //  					</Part>
  //  				</Media>
  //  				<Mood id="131" tag="M&#228;chtig" />
  //  				<Mood id="132" tag="Triumphierend" />
  //  			</Track>
  //  		</MediaContainer>

}
