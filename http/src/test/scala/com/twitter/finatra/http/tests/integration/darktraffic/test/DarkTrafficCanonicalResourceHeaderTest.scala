package com.twitter.finatra.http.tests.integration.darktraffic.test

import com.google.inject.testing.fieldbinder.Bind
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.http.Status._
import com.twitter.finatra.annotations.DarkTrafficService
import com.twitter.finatra.http.{HttpHeaders, EmbeddedHttpServer}
import com.twitter.finatra.http.tests.integration.darktraffic.main.DarkTrafficTestServer
import com.twitter.inject.Mockito
import com.twitter.inject.server.FeatureTest
import com.twitter.util.Future
import org.mockito.ArgumentCaptor

class DarkTrafficCanonicalResourceHeaderTest extends FeatureTest with Mockito {

  @Bind
  @DarkTrafficService
  val darkTrafficService: Option[Service[Request, Response]] = Some(smartMock[Service[Request, Response]])

  darkTrafficService.get.apply(any[Request]).returns(Future.value(smartMock[Response]))

  // receive dark traffic service
  override val server = new EmbeddedHttpServer(
    twitterServer = new DarkTrafficTestServer)

  "DarkTrafficServer" should {

    // Canonical-Resource header is used by Diffy Proxy
    "have Canonical-Resource header correctly set" in {
      server.httpGet (
      "/plaintext",
      withBody = "Hello, World!",
      andExpect = Ok)

      val captor = ArgumentCaptor.forClass (classOf[Request] )
      there was one (darkTrafficService.get).apply (captor.capture () )
      val request = captor.getValue
      request.headerMap (HttpHeaders.CanonicalResource) should be ("GET_/plaintext")
    }
  }
}
