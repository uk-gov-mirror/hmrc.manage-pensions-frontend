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

package controllers.invitations.psp

import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.psa.routes._
import forms.invitations.psp.PspNameFormProvider
import identifiers.{SchemeNameId, SchemeSrnId}
import identifiers.invitations.psp.PspNameId
import models.{Mode, SchemeReferenceNumber}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import utils.annotations.AuthorisePsp
import views.html.invitations.psp.pspName

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PspNameController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   dataCacheConnector: UserAnswersCacheConnector,
                                   @AuthorisePsp navigator: Navigator,
                                   authenticate: AuthAction,
                                   getData: DataRetrievalAction,
                                   requireData: DataRequiredAction,
                                   formProvider: PspNameFormProvider,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: pspName
                                 )(implicit val ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate() andThen getData andThen requireData).async {
    implicit request =>
      (SchemeNameId and SchemeSrnId).retrieve.right.map {
        case schemeName ~ srn =>
          val value = request.userAnswers.get(PspNameId)
          val preparedForm = value.fold(form)(form.fill)

          Future.successful(Ok(view(preparedForm, mode, schemeName, returnCall(srn))))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate() andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          (SchemeNameId and SchemeSrnId).retrieve.right.map { case schemeName ~ srn =>
            Future.successful(BadRequest(view(formWithErrors, mode, schemeName, returnCall(srn))))
          },

        value => {
          dataCacheConnector.save(request.externalId, PspNameId, value).map(
            cacheMap =>
              Redirect(navigator.nextPage(PspNameId, mode, UserAnswers(cacheMap)))
          )
        }
      )
  }

  private def returnCall(srn: String): Call = PsaSchemeDashboardController.onPageLoad(SchemeReferenceNumber(srn))

}
