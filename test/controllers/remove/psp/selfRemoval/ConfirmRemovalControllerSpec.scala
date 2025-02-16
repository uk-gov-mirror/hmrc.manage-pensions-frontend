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

package controllers.remove.psp.selfRemoval

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.remove.psp.ConfirmRemovePspFormProvider
import identifiers.psp.PSPNameId
import identifiers.remove.psp.selfRemoval.ConfirmRemovalId
import identifiers.{SchemeNameId, SchemeSrnId}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.remove.psp.selfRemoval.confirmRemoval

class ConfirmRemovalControllerSpec extends ControllerSpecBase {

  private val formProvider = new ConfirmRemovePspFormProvider()
  private val form = formProvider()

  private def onwardRoute = Call("GET", "/foo")

  private val schemeName = "test-scheme"
  private val pspName = "test-psp-name"
  private val srn = "srn"

  private val data = Json.obj(
    PSPNameId.toString -> pspName,
    SchemeNameId.toString -> schemeName,
    SchemeSrnId.toString -> srn
  )

  private val view = injector.instanceOf[confirmRemoval]

  def controller(dataRetrievalAction: DataRetrievalAction = new FakeDataRetrievalAction(Some(data))) = new ConfirmRemovalController(
    FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, messagesApi, new FakeNavigator(onwardRoute),
    formProvider, FakeUserAnswersCacheConnector, controllerComponents, view)

  private def viewAsString(form: Form[_] = form) = view(form, schemeName, srn, pspName)(fakeRequest, messages).toString

  "Confirm Removal Controller" when {
    "on a GET" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad()(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "populate the view correctly on a GET if the question has previously been answered" in {

        val dataRetrieval = new FakeDataRetrievalAction(Some(data ++ Json.obj(ConfirmRemovalId.toString -> false)))
        val result = controller(dataRetrieval).onPageLoad()(fakeRequest)
        contentAsString(result) mustBe viewAsString(form.fill(false))
      }

      "redirect to the session expired page if there is no required data" in {
        val result = controller(getEmptyData).onPageLoad()(fakeRequest)
        redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "redirect to the session expired page if there is no existing data" in {
        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)
        redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }

    "on a POST" must {
      "save the data and redirect to the next page if valid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

        val result = controller().onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeUserAnswersCacheConnector.verify(ConfirmRemovalId, true)
      }

      "return a Bad Request and errors if invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "yes"))
        val boundForm = form.bind(Map("value" -> "yes"))

        val result = controller().onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "redirect to the session expired page if there is no required data" in {
        val result = controller(getEmptyData).onSubmit()(fakeRequest)
        redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url
      }

      "redirect to the session expired page if there is no existing data" in {
        val result = controller(dontGetAnyData).onSubmit()(fakeRequest)
        redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url
      }
    }
  }

}
