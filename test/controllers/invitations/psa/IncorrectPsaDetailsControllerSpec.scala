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

package controllers.invitations.psa

import controllers.actions._
import controllers.behaviours.ControllerWithNormalPageBehaviours
import models.MinimalSchemeDetail
import play.api.mvc.{Action, AnyContent}
import utils.UserAnswers
import views.html.invitations.psa.incorrectPsaDetails

class IncorrectPsaDetailsControllerSpec extends ControllerWithNormalPageBehaviours {

  val invitee = "PSA"
  val srn = "test-srn"
  val schemeName = "test-scheme-name"
  private val view = injector.instanceOf[incorrectPsaDetails]

  val userAnswer: UserAnswers =
    UserAnswers()
      .inviteeName(invitee)
      .minimalSchemeDetails(MinimalSchemeDetail(srn, None, schemeName))

  def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, fakeAuth: AuthAction): Action[AnyContent] = {

    new IncorrectPsaDetailsController(
      frontendAppConfig, messagesApi, fakeAuth, dataRetrievalAction, requiredDateAction, controllerComponents, view).onPageLoad()
  }

  def viewAsString(): String = view(invitee, srn, schemeName)(fakeRequest, messages).toString

  behave like controllerWithOnPageLoadMethod(onPageLoadAction, getEmptyData, Some(userAnswer.dataRetrievalAction), viewAsString)

}
