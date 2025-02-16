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

package controllers.remove.psa

import connectors.FakeUserAnswersCacheConnector
import controllers.actions.{AuthAction, FakeAuthAction, FakeUnAuthorisedAction}
import controllers.behaviours.ControllerWithNormalPageBehaviours
import models.{Individual, Organization, OtherUser}
import play.api.test.Helpers.{status, _}
import viewmodels.RemovalViewModel
import views.html.remove.psa.cannot_be_removed

class CanNotBeRemovedControllerSpec extends ControllerWithNormalPageBehaviours {

  import CanNotBeRemovedControllerSpec._
  private val view = injector.instanceOf[cannot_be_removed]

  def fakeControllerAction(authAction: AuthAction = FakeUnAuthorisedAction) = new CanNotBeRemovedController(
    messagesApi, authAction, FakeUserAnswersCacheConnector, controllerComponents, view)

  def individualViewAsString(): String = view(viewModelIndividual)(fakeRequest, messages).toString

  def organisationViewAsString(): String = view(viewModelOrganisation)(fakeRequest, messages).toString

  def removalDelayViewAsString(): String = view(viewModelRemovalDelay)(fakeRequest, messages).toString

  "if reason is suspended and affinity group Individual" must {

    "return OK and the correct view for a GET" in {
      val result = fakeControllerAction(FakeAuthAction.createWithUserType(Individual)).onPageLoadWhereSuspended()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe individualViewAsString()
    }
  }

  "if reason is suspended and affinity group Organization" must {

    "return OK and the correct view for a GET" in {
      val result = fakeControllerAction(FakeAuthAction.createWithUserType(Organization)).onPageLoadWhereSuspended()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe organisationViewAsString()
    }
  }

  "if reason is suspended and affinity group is not Individual or Organization" must {

    "redirect to session expired" in {
      val result = fakeControllerAction(FakeAuthAction.createWithUserType(OtherUser)).onPageLoadWhereSuspended()(fakeRequest)

      status(result) mustBe SEE_OTHER
    }

    "return 303 if user action is not authenticated" in {

      val result = fakeControllerAction().onPageLoadWhereSuspended()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
    }
  }

  "if reason is removal delay" must {
    "return OK and the correct view for a GET" in {
      val result = fakeControllerAction(FakeAuthAction.createWithUserType(Individual)).onPageLoadWhereRemovalDelay()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe removalDelayViewAsString()
    }
  }

}

object CanNotBeRemovedControllerSpec {

  private val viewModelIndividual: RemovalViewModel = RemovalViewModel(
    "messages__you_cannot_be_removed__title",
    "messages__you_cannot_be_removed__heading",
    "messages__you_cannot_be_removed__p1",
    "messages__you_cannot_be_removed__p2",
    "messages__you_cannot_be_removed__returnToSchemes__link")

  private val viewModelOrganisation: RemovalViewModel = RemovalViewModel(
    "messages__psa_cannot_be_removed__title",
    "messages__psa_cannot_be_removed__heading",
    "messages__psa_cannot_be_removed__p1",
    "messages__psa_cannot_be_removed__p2",
    "messages__psa_cannot_be_removed__returnToSchemes__link")

  private def viewModelRemovalDelay: RemovalViewModel = RemovalViewModel(
    "messages__psa_cannot_be_removed__title",
    "messages__psa_cannot_be_removed__heading",
    "messages__psa_cannot_be_removed_delay__p1",
    "messages__psa_cannot_be_removed_delay__p2",
    "messages__psa_cannot_be_removed__returnToSchemes__link")
}
