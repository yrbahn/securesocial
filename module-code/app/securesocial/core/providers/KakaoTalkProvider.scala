/**
 * Copyright 2012-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package securesocial.core.providers

import play.api.Logger
import play.api.libs.json._
import securesocial.core._
import play.api.libs.ws.Response
import scala.concurrent.{ExecutionContext, Future}
import securesocial.core.services.{RoutesService, CacheService, HttpService}

/**
 * A Facebook Provider
 */
class KakaoTalkProvider(routesService: RoutesService,
                       httpService: HttpService,
                       cacheService: CacheService,
                       settings: OAuth2Settings = OAuth2Settings.forProvider(KakaoTalkProvider.KakaoTalk))
  extends OAuth2Provider(settings, routesService, httpService, cacheService)
{
  val MeApi = "https://kapi.kakao.com/v1/user/me"
  val uId = "id"
  val Properties = "properties"
  val NickName = "nickname"
  val Picture = "thumbnail_image"
  val AccessToken = "access_token"
  val Expires = "expires"
  val Data = "data"
  val Url = "profile_image"

  override val id = KakaoTalkProvider.KakaoTalk

  def fillProfile(info: OAuth2Info): Future[BasicProfile] = {
    import ExecutionContext.Implicits.global
    val accessToken = info.accessToken
    httpService.url(MeApi).withHeaders("Authorization" -> ("Bearer %s" format(accessToken)),
      "Content-Type" -> "application/x-www-form-urlencoded").get().map {
      response =>
        val me : JsValue = response.json
        val userId = (me \ uId).as[Int].toString()
        val properties = ( me \ Properties).as[JsObject]
        val nickname = (properties \ NickName).asOpt[String]
        val avatarUrl = (properties \ Url).asOpt[String]
        BasicProfile(id, userId, None, None, nickname, Some(userId), avatarUrl, authMethod, oAuth2Info = Some(info))

    } recover {
      case e: AuthenticationException => throw e
      case e =>
        Logger.error("[securesocial] error retrieving profile information from kakaoTalk",  e)
        throw new AuthenticationException()
    }
  }

}

object KakaoTalkProvider {
  val KakaoTalk = "kakaotalk"
}
