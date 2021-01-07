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

package controllers.triage

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.TriageAction
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.triage.updateBoth

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UpdateBothController @Inject()(
                                          frontendAppConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          triageAction: TriageAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: updateBoth
                                        )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = triageAction.async {
    implicit request =>
      val managePensionSchemesServiceLink = s"${frontendAppConfig.loginUrl}?continue=${frontendAppConfig.registeredPsaDetailsUrl}"
      val pensionSchemesOnlineServiceLink = frontendAppConfig.tpssInitialQuestionsUrl

      Future.successful(Ok(view(managePensionSchemesServiceLink, pensionSchemesOnlineServiceLink)))
  }
}
