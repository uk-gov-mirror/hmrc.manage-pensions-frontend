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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.triage.deAuthoriseYourself

import scala.concurrent.{ExecutionContext, Future}

class DeAuthoriseYourselfController @Inject()(frontendAppConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              triageAction: TriageAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: deAuthoriseYourself
                                              )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = triageAction.async {
    implicit request =>

      val continueLink = s"${frontendAppConfig.loginUrl}?continue=${frontendAppConfig.loginToListSchemesPspUrl}"
      Future.successful(Ok(view(continueLink)))
  }
}
