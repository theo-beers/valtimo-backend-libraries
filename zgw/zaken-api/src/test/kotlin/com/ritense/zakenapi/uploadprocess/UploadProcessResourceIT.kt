/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.uploadprocess

import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.uploadprocess.ResourceUploadedEventListener.Companion.UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import javax.transaction.Transactional

@Transactional
class UploadProcessResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should respond with no process-case-link when none has been configured`() {
        mockMvc.perform(get("/api/uploadprocess/case/{caseDefinitionKey}/check-link", CASE_DEFINITION_KEY))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.processCaseLinkExists").value(false))
    }

    @Test
    fun `should respond with process-case-link when one has been configured`() {
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY,
                CASE_DEFINITION_KEY,
                true,
            )
        )

        mockMvc.perform(get("/api/uploadprocess/case/{caseDefinitionKey}/check-link", CASE_DEFINITION_KEY))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.processCaseLinkExists").value(true))
    }

    companion object {
        private const val CASE_DEFINITION_KEY = "profile"
    }
}