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

package com.ritense.zakenapi.web.rest

import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.domain.RelatedFileDto
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.UUID

class ZaakDocumentResourceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should get zaak-documenten by document`() {
        val documentId = UUID.randomUUID()

        val informatieObjectId = UUID.randomUUID()
        val relatedFile = createRelatedFile(URI("https://example.local/$informatieObjectId"))
        doReturn(
            listOf(
                relatedFile
            )
        ).whenever(zaakDocumentService).getInformatieObjectenAsRelatedFiles(documentId)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/zaken-api/document/{documentId}/files", documentId)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$.[0].pluginConfigurationId").value(relatedFile.pluginConfigurationId.toString()))
            .andExpect(jsonPath("$.[0].fileName").value(relatedFile.fileName))
            .andExpect(jsonPath("$.[0].sizeInBytes").value(relatedFile.sizeInBytes))
            .andExpect(jsonPath("$.[0].createdOn").value(relatedFile.createdOn.toString()))
            .andExpect(jsonPath("$.[0].createdBy").value(relatedFile.createdBy))
            .andExpect(jsonPath("$.[0].fileId").value(relatedFile.fileId.toString()))
    }

    private fun createRelatedFile(uri: URI) = RelatedFileDto(
        fileId = UUID.fromString(uri.path.substringAfterLast("/")),
        fileName = "titel",
        sizeInBytes = 1337L,
        createdOn = LocalDateTime.now(),
        createdBy = "y",
        pluginConfigurationId = UUID.fromString("1f925112-f090-404a-bee7-b20fd8047a72"),
    )
}