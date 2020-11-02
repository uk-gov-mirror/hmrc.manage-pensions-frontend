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

package controllers.invitations.psp

import com.google.inject.Inject
import connectors.admin.MinimalConnector
import connectors.scheme.ListOfSchemesConnector
import connectors.{ActiveRelationshipExistsException, EmailConnector, EmailSent, PspConnector}
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction, IdNotFound}
import forms.invitations.psp.DeclarationFormProvider
import identifiers.invitations.psp.{PspClientReferenceId, PspId, PspNameId}
import identifiers.{SchemeNameId, SchemeSrnId}
import models.SendEmailRequest
import models.invitations.psp.ClientReference
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SchemeDetailsService
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.invitations.psp.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(override val messagesApi: MessagesApi,
                                      formProvider: DeclarationFormProvider,
                                      auth: AuthAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      pspConnector: PspConnector,
                                      listOfSchemesConnector: ListOfSchemesConnector,
                                      schemeDetailsService: SchemeDetailsService,
                                      emailConnector: EmailConnector,
                                      minimalConnector: MinimalConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: declaration
                                     )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {
  val form: Form[Boolean] = formProvider()
  val sessionExpired: Future[Result] = Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))

  def onPageLoad(): Action[AnyContent] = (auth() andThen getData andThen requireData) {
    implicit request =>
          Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = (auth() andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[Boolean]) =>
          Future.successful(BadRequest(view(formWithErrors))),
        _ => inviteEmailAndRedirect()
      )
   }

  private def inviteEmailAndRedirect()(implicit request: DataRequest[AnyContent]): Future[Result] =
    (SchemeNameId and SchemeSrnId and PspNameId and PspId and PspClientReferenceId).retrieve.right.map {
      case schemeName ~ srn ~ pspName ~ pspId ~ pspCR =>
        getPstr(srn).flatMap {
          case Some(pstr) =>
            pspConnector.authorisePsp(pstr, pspName, pspId, getClientReference(pspCR)).flatMap { _ =>
              sendEmail(request.psaId, pspName, schemeName).map { _ =>
                Redirect(routes.ConfirmationController.onPageLoad())
              }
            } recoverWith {
              case _: ActiveRelationshipExistsException =>
                Future.successful(Redirect(controllers.invitations.psp.routes.AlreadyAssociatedWithSchemeController.onPageLoad()))
            }
          case _ => sessionExpired
        }
    }.left.map(_ => sessionExpired)


  private def getPstr(srn: String)(implicit request: DataRequest[AnyContent]): Future[Option[String]] =
    listOfSchemesConnector.getListOfSchemes(request.psaIdOrException.id).map {
      case Right(list) => schemeDetailsService.pstr(srn, list)
      case _ => None
    }

  private def getClientReference(answer: ClientReference): Option[String] = answer match {
    case ClientReference.HaveClientReference(reference) => Some(reference)
    case ClientReference.NoClientReference => None
  }

  private def sendEmail(psaIdOpt: Option[PsaId], pspName: String, schemeName: String)
                              (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val psaId: PsaId = psaIdOpt.getOrElse(throw IdNotFound())
    minimalConnector.getMinimalPsaDetails(psaId.id).map { psa =>

      val email = SendEmailRequest(
        List(psa.email),
        "pods_authorise_psp",
        Map(
          "psaInvitor" -> psa.name,
          "pspInvitee" -> pspName,
          "schemeName" -> schemeName
        ),
        force = false,
        None
      )

      emailConnector.sendEmail(email).map {
        case EmailSent => println("\n\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>EmailSent")
          ()
        case _ =>
          Logger.error("Unable to send email to authorising PSA. Support intervention possibly required.")
          ()
      }
    }
  }
}
