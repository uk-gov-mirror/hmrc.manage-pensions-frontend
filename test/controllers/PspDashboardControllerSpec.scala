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

package controllers

import config._
import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, _}
import models.{IndividualDetails, Link, MinimalPSAPSP}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsString, _}
import services.PspDashboardService
import viewmodels.{CardSubHeading, CardSubHeadingParam, CardViewModel, Message}
import views.html.schemesOverview

import scala.concurrent.Future

class PspDashboardControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  import PspDashboardControllerSpec._

  private val mockPspDashboardService: PspDashboardService = mock[PspDashboardService]
  private val mockUserAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private def minimalPsaDetails(rlsFlag: Boolean): MinimalPSAPSP =
    MinimalPSAPSP(
      email = "test@test.com",
      isPsaSuspended = false,
      organisationName = None,
      individualDetails = Some(IndividualDetails("Test", None, "Psp Name")),
      rlsFlag = rlsFlag
    )

  private val view: schemesOverview = app.injector.instanceOf[schemesOverview]
  private val dummyUrl = "dummy"

  def controller(dataRetrievalAction: DataRetrievalAction = dontGetAnyDataPsp): PspDashboardController =
    new PspDashboardController(
      messagesApi = messagesApi,
      service = mockPspDashboardService,
      authenticate = FakeAuthAction,
      getData = dataRetrievalAction,
      userAnswersCacheConnector = mockUserAnswersCacheConnector,
      controllerComponents = controllerComponents,
      view = view,
      config = mockAppConfig
    )

  def viewAsString(): String = view(
    name = pspName,
    cards = tiles,
    subHeading = Some(subHeading),
    returnLink = Some(returnLink)
  )(
    fakeRequest,
    messages
  ).toString

  private val practitionerCard: CardViewModel =
    CardViewModel(
      id = "practitioner-card",
      heading = Message("messages__pspDashboard__details_heading"),
      subHeadings = Seq(
        CardSubHeading(
          subHeading = Message("messages__pspDashboard__psp_id"),
          subHeadingClasses = "heading-small card-sub-heading",
          subHeadingParams = Seq(
            CardSubHeadingParam(
              subHeadingParam = pspId,
              subHeadingParamClasses = "font-small")))),
      links = Seq(
        Link(
          id = "pspLink",
          url = frontendAppConfig.pspDetailsUrl,
          linkText = Message("messages__pspDashboard__psp_change")
        ),
        Link(
          id = "deregister-link",
          url = frontendAppConfig.pspDeregisterIndividualUrl,
          linkText = Message("messages__pspDashboard__psp_deregister")
        )
      )
    )

  private def schemeCard: CardViewModel =
    CardViewModel(
      id = "scheme-card",
      heading = Message("messages__pspDashboard__scheme_heading"),
      links = Seq(
        Link(
          id = "search-schemes",
          url = controllers.routes.ListSchemesController.onPageLoad().url,
          linkText = Message("messages__pspDashboard__search_scheme")
        )
      )
    )

  private val tiles: Seq[CardViewModel] = Seq(schemeCard, practitionerCard)
  val subHeading: String = Message("messages__pspDashboard__sub_heading")

  "PspDashboard Controller" when {
    "onPageLoad" must {
      "return OK and the correct tiles" in {
        when(mockPspDashboardService.getTiles(eqTo(pspId), any())(any()))
          .thenReturn(tiles)
        when(mockPspDashboardService.getPspDetails(eqTo(pspId))(any()))
          .thenReturn(Future.successful(minimalPsaDetails(rlsFlag = false)))
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Json.obj()))

        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to update contact details page when rls flag is true" in {
        when(mockPspDashboardService.getTiles(eqTo(pspId), any())(any()))
          .thenReturn(tiles)
        when(mockPspDashboardService.getPspDetails(eqTo(pspId))(any()))
          .thenReturn(Future.successful(minimalPsaDetails(rlsFlag = true)))
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Json.obj()))
        when(mockAppConfig.pspUpdateContactDetailsUrl) thenReturn dummyUrl

        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(dummyUrl)
      }

    }
  }
}

object PspDashboardControllerSpec {
  val pspName = "Test Psp Name"
  private val pspId = "00000000"
  private val returnLink: Link =
    Link(
      id = "switch-psa",
      url = routes.SchemesOverviewController.onPageLoad().url,
      linkText = Message("messages__pspDashboard__switch_psa")
    )
}




