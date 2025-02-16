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

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.invitations.psa.PsaIdFormProvider
import identifiers.invitations.InviteeNameId
import identifiers.invitations.psa.InviteePSAId
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Invitations
import utils.{Navigator, UserAnswers}
import views.html.invitations.psa.psaId

import scala.concurrent.{ExecutionContext, Future}


class PsaIdController @Inject()(
                                 override val messagesApi: MessagesApi,
                                 authenticate: AuthAction,
                                 @Invitations navigator: Navigator,
                                 dataCacheConnector: UserAnswersCacheConnector,
                                 getData: DataRetrievalAction,
                                 requireData: DataRequiredAction,
                                 formProvider: PsaIdFormProvider,
                                 val controllerComponents: MessagesControllerComponents,
                                 view: psaId
                               )(implicit val ec: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate() andThen getData andThen requireData).async {
      implicit request =>

        InviteeNameId.retrieve.right.map {
          psaName =>
            val value = request.userAnswers.get(InviteePSAId)
            val preparedForm = value.fold(form)(form.fill)

            Future.successful(Ok(view(preparedForm, psaName, mode)))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate() andThen getData andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            InviteeNameId.retrieve.right.map {
              psaName =>
                Future.successful(BadRequest(view(formWithErrors, psaName, mode)))
            },
          value =>
            dataCacheConnector.save(request.externalId, InviteePSAId, value).map(
              cacheMap =>
                Redirect(navigator.nextPage(InviteePSAId, mode, UserAnswers(cacheMap)))
            )
        )

    }
}
