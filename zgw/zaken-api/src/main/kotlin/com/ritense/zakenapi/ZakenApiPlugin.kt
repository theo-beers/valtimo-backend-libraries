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

package com.ritense.zakenapi

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.zakenapi.client.LinkDocumentRequest
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zgw.Page
import java.net.URI
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.UUID

@Plugin(
    key = ZakenApiPlugin.PLUGIN_KEY,
    title = "Zaken API",
    description = "Connects to the Zaken API"
)
class ZakenApiPlugin(
    private val client: ZakenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val resourceProvider: ResourceProvider,
    private val documentService: DocumentService,
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI
    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: ZakenApiAuthentication

    @PluginAction(
        key = "link-document-to-zaak",
        title = "Link Documenten API document to Zaak",
        description = "Stores a link to an existing document in the Documenten API with a Zaak",
        activityTypes = [ActivityType.SERVICE_TASK]
    )
    fun linkDocumentToZaak(
        execution: DelegateExecution,
        @PluginActionProperty documentUrl: String,
        @PluginActionProperty titel: String?,
        @PluginActionProperty beschrijving: String?
    ){
        val documentId = UUID.fromString(execution.businessKey)
        val zaakUrl = zaakUrlProvider.getZaak(documentId)

        val request = LinkDocumentRequest(
            documentUrl,
            zaakUrl,
            titel,
            beschrijving
        )

        client.linkDocument(authenticationPluginConfiguration, url, request)
        val resource = resourceProvider.getResource(documentUrl)
        documentService.assignResource(
            JsonSchemaDocumentId.existingId(documentId),
            resource.id(),
            mapOf("createInformatieObject" to false)
        )
    }

    fun getZaakObjecten(zaakUrl: URI): List<ZaakObject> {
        var currentPage = 1
        var currentResults: Page<ZaakObject>?
        val results = mutableListOf<ZaakObject>()

        do {
            currentResults = client.getZaakObjecten(
                authenticationPluginConfiguration,
                url,
                zaakUrl,
                currentPage++
            )
            results.addAll(currentResults.results)
        } while(currentResults?.next != null)

        return results
    }

    companion object {
        const val PLUGIN_KEY = "zakenapi"
    }
}