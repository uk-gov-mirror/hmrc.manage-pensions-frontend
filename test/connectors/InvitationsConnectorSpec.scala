/*
 * Copyright 2021 HM Revenue & Customs
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
 */

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.invitations.psp
import models.{AcceptedInvitation, SchemeReferenceNumber}
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatestplus.scalacheck.Checkers
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.WireMockHelper

import java.time.LocalDate

class InvitationsConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper with Checkers {

  import InvitationsConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "invite" should "return successfully following a successful invite" in {

    server.stubFor(
      post(urlEqualTo(inviteUrl))
        .withRequestBody(equalToJson(requestJson))
        .willReturn(
          aResponse()
            .withStatus(Status.CREATED)
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    connector.invite(invitation).map(
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(inviteUrl))).size() shouldBe 1
    )

  }

  it should "throw NameMatchingFailedException for a Not Found response if name matching fails" in {

    server.stubFor(
      post(urlEqualTo(inviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
            .withBody(namePsaIdDoNotMatchResponse)
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[NameMatchingFailedException] {
      connector.invite(invitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(inviteUrl))).size() shouldBe 1
    }

  }

  it should "throw PsaAlreadyInvitedException for a Forbidden response if Psa already invited" in {

    server.stubFor(
      post(urlEqualTo(inviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.FORBIDDEN)
            .withBody(psaAlreadyInvitedResponse)
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[PsaAlreadyInvitedException] {
      connector.invite(invitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(inviteUrl))).size() shouldBe 1
    }

  }

  "acceptInvite" should "return successfully when user accepts the invite" in {

    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .withRequestBody(equalToJson(Json.stringify(Json.toJson(acceptedInvitation))))
        .willReturn(
          aResponse()
            .withStatus(Status.CREATED)
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    connector.acceptInvite(acceptedInvitation) map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw PstrInvalidException for a Bad Request (INVALID_PSTR) response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("INVALID_PSTR"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[PstrInvalidException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw InvalidInvitationPayloadException for a Bad Request (INVALID_PAYLOAD) response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("INVALID_PAYLOAD"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[InvalidInvitationPayloadException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw InviteePsaIdInvalidException for a Bad Request (INVALID_INVITEE_PSAID) response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("INVALID_INVITEE_PSAID"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[InviteePsaIdInvalidException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw InviterPsaIdInvalidException for a Bad Request (INVALID_INVITER_PSAID) response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("INVALID_INVITER_PSAID"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[InviterPsaIdInvalidException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw ActiveRelationshipExistsException for a Conflict (ACTIVE_RELATIONSHIP_EXISTS) response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.CONFLICT)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("ACTIVE_RELATIONSHIP_EXISTS"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[ActiveRelationshipExistsException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

  it should "throw NotFoundException for a Not Found response" in {
    server.stubFor(
      post(urlEqualTo(acceptInviteUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidResponse("NOT_FOUND"))
        )
    )

    val connector = injector.instanceOf[InvitationConnector]

    recoverToExceptionIf[NotFoundException] {
      connector.acceptInvite(acceptedInvitation)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(acceptInviteUrl))).size() shouldBe 1
    }
  }

}

object InvitationsConnectorSpec {

  private val inviteUrl = "/pension-administrator/invite"
  private val acceptInviteUrl = "/pension-administrator/accept-invitation"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val srn = SchemeReferenceNumber("S0987654321")
  private val pstr = "test-pstr"
  private val schemeName = "test-scheme-name"
  private val inviterPsaId = PsaId("A7654321")
  private val inviteePsaId = PsaId("A1234567")
  private val inviteeName = "test-invitee-name"
  private val expireAt = LocalDate.parse("2018-05-05").atStartOfDay()
  private val declaration = true
  private val declarationDuties = true

  private val acceptedInvitation = AcceptedInvitation(
    pstr,
    inviteePsaId,
    inviterPsaId,
    declaration,
    declarationDuties,
    None
  )

  private val invitation =
    psp.Invitation(
      srn,
      pstr,
      schemeName,
      inviterPsaId,
      inviteePsaId,
      inviteeName,
      expireAt
    )

  private val requestJson =
    Json.stringify(
      Json.toJson(invitation)
    )

  private val namePsaIdDoNotMatchResponse = "The name and PSA Id do not match"
  private val psaAlreadyInvitedResponse = "The invitation is to a PSA already associated with this scheme"

  private def invalidResponse(code: String) =
    Json.stringify(
      Json.obj(
        "code" -> code,
        "reason" -> s"Reason for $code"
      )
    )

}
