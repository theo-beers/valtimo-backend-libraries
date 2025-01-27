/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.smartdocuments

import com.ritense.connector.autodeployment.ConnectorApplicationReadyEventListener
import com.ritense.resource.service.ResourceService
import com.ritense.testutilscommon.junit.extension.LiquibaseRunnerExtension
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.service.CurrentUserService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(value = [SpringExtension::class, LiquibaseRunnerExtension::class])
@Tag("integration")
abstract class BaseIntegrationTest : BaseTest() {

    @MockBean
    lateinit var connectorApplicationReadyEventListener: ConnectorApplicationReadyEventListener

    @MockBean
    lateinit var userManagementService: UserManagementService

    @MockBean
    lateinit var currentUserService: CurrentUserService

    @MockBean
    lateinit var resourceService: ResourceService

    @MockBean
    lateinit var mailSender: MailSender
}