/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.psp

import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import connectors.admin.MinimalConnector
import controllers.ControllerSpecBase
import controllers.actions.AuthAction
import controllers.actions.FakeAuthAction
import forms.psp.ListSchemesFormProvider
import models.SchemeDetails
import models.SchemeStatus
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import services.SchemeSearchService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.psp.list_schemes

import scala.concurrent.Future

class ListSchemesControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {
import ListSchemesControllerSpec._

  override def beforeEach(): Unit = {
    when(mockAppConfig.minimumSchemeSearchResults) thenReturn 1
  }

  "onPageLoad" when {

    when(mockMinimalConnector.getNameFromPspID(any())(any(), any())).thenReturn(Future.successful(Some(pspName)))

    "return OK and the correct view when there are no schemes" in {
      when(mockSchemeSearchService.searchPsp(any(), any())(any(), any())).thenReturn(Future.successful(Nil))

      val fixture = testFixture(psaIdNoSchemes)

      val result = fixture.controller.onPageLoad(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(
        schemes = emptySchemes,
        numberOfSchemes = emptySchemes.length,
        formValue = None
      )
    }
  }

  "onSearch" when {

    when(mockMinimalConnector.getNameFromPspID(any())(any(), any()))
      .thenReturn(Future.successful(Some(pspName)))

    "return OK and the correct view when there are schemes" in {
      val searchText = "24000001IN"
      when(mockSchemeSearchService.searchPsp(any(), Matchers.eq(Some(searchText)))(any(), any())).thenReturn(Future.successful(fullSchemes))

      val fixture = testFixture(psaIdWithSchemes)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("searchText", searchText))
      val result = fixture.controller.onSearch(postRequest)

      status(result) mustBe OK

      val expected = viewAsString(
        schemes = fullSchemes,
        numberOfSchemes = fullSchemes.length,
        Some(searchText)
      )

      contentAsString(result) mustBe expected
    }

    "return BADREQUEST and error when no value is entered into search" in {
      when(mockSchemeSearchService.searchPsp(any(), Matchers.eq(None))(any(), any())).thenReturn(Future.successful(fullSchemes))

      val fixture = testFixture(psaIdWithSchemes)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("searchText", ""))
      val result = fixture.controller.onSearch(postRequest)

      status(result) mustBe BAD_REQUEST

      val expected = viewAsString(
        schemes = fullSchemes,
        numberOfSchemes = fullSchemes.length,
        Some("")
      )

      contentAsString(result) mustBe expected
    }

      "return OK and the correct view with correct no matches message when no match is made" in {

        val incorrectSearchText = "incorrect"
        when(mockSchemeSearchService.searchPsp(any(), Matchers.eq(Some(incorrectSearchText)))(any(), any())).thenReturn(Future.successful(Nil))

        val fixture = testFixture(psaIdWithSchemes)
        val postRequest =
          fakeRequest.withFormUrlEncodedBody(("searchText", incorrectSearchText))
        val result = fixture.controller.onSearch(postRequest)

        status(result) mustBe OK

        val expected = viewAsString(
          schemes = List.empty,
          numberOfSchemes = 0,
          Some(incorrectSearchText)
        )

        contentAsString(result) mustBe expected
      }
  }
}

trait TestFixture {
  def controller: ListSchemesController
}

object ListSchemesControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val psaIdNoSchemes: String = "20000001"
  private val psaIdWithSchemes: String = "20000002"
  private val pspName: String = "Test Psa Name"
  private val emptySchemes: List[SchemeDetails] = List.empty[SchemeDetails]
  private val mockMinimalConnector: MinimalConnector =
    mock[MinimalConnector]
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private val listSchemesFormProvider = new ListSchemesFormProvider
  private val mockSchemeSearchService = mock[SchemeSearchService]

  private val fullSchemes: List[SchemeDetails] =
    List(
      SchemeDetails(
        name = "scheme-0",
        referenceNumber = "srn-0",
        schemeStatus = SchemeStatus.Open.value,
        openDate = None,
        pstr = Some("pstr-0"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-1",
        referenceNumber = "srn-1",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("24000001IN"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-2",
        referenceNumber = "S2400000005",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-2"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-3",
        referenceNumber = "srn-3",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-3"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-4",
        referenceNumber = "srn-4",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-4"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-5",
        referenceNumber = "srn-5",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-5"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-6",
        referenceNumber = "srn-6",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-6"),
        relationship = None,
        underAppeal = None
      ),
      SchemeDetails(
        name = "scheme-7",
        referenceNumber = "srn-7",
        schemeStatus = SchemeStatus.Deregistered.value,
        openDate = None,
        pstr = Some("pstr-7"),
        relationship = None,
        underAppeal = None
      )
    )
  private val view: list_schemes = app.injector.instanceOf[list_schemes]

  private def testFixture(psaId: String): TestFixture =
    new TestFixture with MockitoSugar {

      private def authAction(psaId: String): AuthAction =
        FakeAuthAction.createWithPspId(psaId)

      override val controller: ListSchemesController =
        new ListSchemesController(
          mockAppConfig,
          messagesApi,
          authAction(psaId),
          getDataWithPspName(psaId),
          mockMinimalConnector,
          FakeUserAnswersCacheConnector,
          stubMessagesControllerComponents(),
          view,
          listSchemesFormProvider,
          mockSchemeSearchService
        )
    }

  private def viewAsString(schemes: List[SchemeDetails],
                           numberOfSchemes: Int,
                           formValue: Option[String]): String = {

    view(
      form = formValue
        .fold(listSchemesFormProvider())(v => listSchemesFormProvider().bind(Map("searchText" -> v))),
      schemes = schemes,
      psaName = pspName,
      numberOfSchemes = numberOfSchemes
    )(fakeRequest, messages).toString()
  }
}

