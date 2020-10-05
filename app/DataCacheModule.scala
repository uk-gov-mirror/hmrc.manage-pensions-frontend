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

import connectors._
import play.api.inject.Binding
import play.api.inject.Module
import play.api.Configuration
import play.api.Environment
import utils.annotations.PensionAdminCache

class DataCacheModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[UserAnswersCacheConnector].to[ManagePensionsCacheConnector],
      bind[InvitationsCacheConnector].to[InvitationsCacheConnectorImpl],
      bind[UserAnswersCacheConnector].qualifiedWith(classOf[PensionAdminCache]).to[admin.PensionAdminCacheConnector]
    )
  }
}
