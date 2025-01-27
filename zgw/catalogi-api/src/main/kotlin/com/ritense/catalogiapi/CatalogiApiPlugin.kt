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

package com.ritense.catalogiapi

import com.ritense.catalogiapi.client.BesluittypeRequest
import com.ritense.catalogiapi.client.CatalogiApiClient
import com.ritense.catalogiapi.client.ResultaattypeRequest
import com.ritense.catalogiapi.client.RoltypeRequest
import com.ritense.catalogiapi.client.StatustypeRequest
import com.ritense.catalogiapi.client.ZaaktypeInformatieobjecttypeRequest
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.zgw.Page
import mu.KotlinLogging
import java.net.URI

@Plugin(
    key = "catalogiapi",
    title = "Catalogi API",
    description = "Connects to the Catalogi API to retrieve zaak type information"
)
class CatalogiApiPlugin(
    val client: CatalogiApiClient
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: CatalogiApiAuthentication

    fun getInformatieobjecttypes(
        zaakTypeUrl: URI,
    ): List<Informatieobjecttype> {
        var currentPage = 1
        var currentResults: Page<ZaaktypeInformatieobjecttype>?
        val results = mutableListOf<Informatieobjecttype>()

        do {
            logger.debug { "Getting page of ZaaktypeInformatieobjecttypes, page $currentPage for zaaktype $zaakTypeUrl" }
            currentResults = client.getZaaktypeInformatieobjecttypes(
                authenticationPluginConfiguration,
                url,
                ZaaktypeInformatieobjecttypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = currentPage++
                )
            )
            currentResults.results.map {
                logger.trace { "Getting Informatieobjecttype ${it.informatieobjecttype}" }
                val informatieobjecttype = client.getInformatieobjecttype(
                    authenticationPluginConfiguration,
                    url,
                    it.informatieobjecttype
                )
                results.add(informatieobjecttype)
            }
        } while(currentResults?.next != null)

        return results
    }

    fun getRoltypes(zaakTypeUrl: URI): List<Roltype> {
        var currentPage = 1
        var currentResults: Page<Roltype>?
        val results = mutableListOf<Roltype>()

        do {
            logger.debug { "Getting page of Roltypes, page $currentPage for zaaktype $zaakTypeUrl" }
            currentResults = client.getRoltypen(
                authenticationPluginConfiguration,
                url,
                RoltypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = currentPage++
                )
            )
            results.addAll(currentResults.results)
        } while(currentResults?.next != null)

        return results
    }

    fun getStatustypen(zaakTypeUrl: URI): List<Statustype> {
        var currentPage = 1
        var currentResults: Page<Statustype>?
        val results = mutableListOf<Statustype>()

        do {
            logger.debug { "Getting page of statustypen, page $currentPage for zaaktype $zaakTypeUrl" }
            currentResults = client.getStatustypen(
                authenticationPluginConfiguration,
                url,
                StatustypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = currentPage++
                )
            )
            results.addAll(currentResults.results)
        } while(currentResults?.next != null)

        return results
    }

    fun getResultaattypen(zaakTypeUrl: URI): List<Resultaattype> {
        var currentPage = 1
        var currentResults: Page<Resultaattype>?
        val results = mutableListOf<Resultaattype>()

        do {
            logger.debug { "Getting page of resultaattypen, page $currentPage for zaaktype $zaakTypeUrl" }
            currentResults = client.getResultaattypen(
                authenticationPluginConfiguration,
                url,
                ResultaattypeRequest(
                    zaaktype = zaakTypeUrl,
                    page = currentPage++
                )
            )
            results.addAll(currentResults.results)
        } while(currentResults?.next != null)

        return results
    }

    fun getBesluittypen(zaakTypeUrl: URI): List<Besluittype> {
        var currentPage = 1
        var currentResults: Page<Besluittype>?
        val results = mutableListOf<Besluittype>()

        do {
            logger.debug { "Getting page of besluittypen, page $currentPage for zaaktype $zaakTypeUrl" }
            currentResults = client.getBesluittypen(
                authenticationPluginConfiguration,
                url,
                BesluittypeRequest(
                    zaaktypen = zaakTypeUrl,
                    page = currentPage++
                )
            )
            results.addAll(currentResults.results)
        } while(currentResults?.next != null)

        return results
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
