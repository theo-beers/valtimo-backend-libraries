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

import com.ritense.catalogiapi.client.ZaaktypeInformatieobjecttypeRequest
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.catalogiapi.client.CatalogiApiClient
import com.ritense.zgw.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI
import kotlin.test.assertEquals

internal class CatalogiApiPluginTest{

    val client = mock<CatalogiApiClient>()
    val plugin = CatalogiApiPlugin(client)

    @BeforeEach
    fun setUp() {
        plugin.authenticationPluginConfiguration = mock()
        plugin.url = mock()
    }

    @Test
    fun `should call client to get informatieobjecttypes`() {
        val zaakTypeUrl = URI("https://example.com/zaaktype")
        val resultPage = mock<Page<ZaaktypeInformatieobjecttype>>()
        whenever(client.getZaaktypeInformatieobjecttypes(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            ZaaktypeInformatieobjecttypeRequest(
                zaaktype = zaakTypeUrl,
                page = 1
            )
        )).thenReturn(resultPage)

        val mockZaaktypeInformatieobjecttype1 = mock<ZaaktypeInformatieobjecttype>()
        val mockZaaktypeInformatieobjecttype2 = mock<ZaaktypeInformatieobjecttype>()
        whenever(resultPage.results).thenReturn(listOf(mockZaaktypeInformatieobjecttype1,
            mockZaaktypeInformatieobjecttype2))

        val mockInformatieobjecttype1 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl1 = URI("https://example.com/informatieobjecttype/1")
        whenever(mockZaaktypeInformatieobjecttype1.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl1)
        val mockInformatieobjecttype2 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl2 = URI("https://example.com/informatieobjecttype/2")
        whenever(mockZaaktypeInformatieobjecttype2.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl2)

        whenever(client.getInformatieobjecttype(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            mockInformatieobjecttypeUrl1
        )).thenReturn(mockInformatieobjecttype1)

        whenever(client.getInformatieobjecttype(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            mockInformatieobjecttypeUrl2
        )).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

    @Test
    fun `should call client to get informatieobjecttypes with multiple pages`() {
        val zaakTypeUrl = URI("https://example.com/zaaktype")
        val resultPage1 = mock<Page<ZaaktypeInformatieobjecttype>>()
        whenever(resultPage1.next).thenReturn(URI("https://example.com/zaaktype/2"))
        val resultPage2 = mock<Page<ZaaktypeInformatieobjecttype>>()

        whenever(client.getZaaktypeInformatieobjecttypes(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            ZaaktypeInformatieobjecttypeRequest(
                zaaktype = zaakTypeUrl,
                page = 1
            )
        )).thenReturn(resultPage1)

        whenever(client.getZaaktypeInformatieobjecttypes(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            ZaaktypeInformatieobjecttypeRequest(
                zaaktype = zaakTypeUrl,
                page = 2
            )
        )).thenReturn(resultPage2)

        val mockZaaktypeInformatieobjecttype1 = mock<ZaaktypeInformatieobjecttype>()
        val mockZaaktypeInformatieobjecttype2 = mock<ZaaktypeInformatieobjecttype>()
        whenever(resultPage1.results).thenReturn(listOf(mockZaaktypeInformatieobjecttype1))
        whenever(resultPage2.results).thenReturn(listOf(mockZaaktypeInformatieobjecttype2))

        val mockInformatieobjecttype1 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl1 = URI("https://example.com/informatieobjecttype/1")
        whenever(mockZaaktypeInformatieobjecttype1.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl1)
        val mockInformatieobjecttype2 = mock<Informatieobjecttype>()
        val mockInformatieobjecttypeUrl2 = URI("https://example.com/informatieobjecttype/2")
        whenever(mockZaaktypeInformatieobjecttype2.informatieobjecttype)
            .thenReturn(mockInformatieobjecttypeUrl2)

        whenever(client.getInformatieobjecttype(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            mockInformatieobjecttypeUrl1
        )).thenReturn(mockInformatieobjecttype1)

        whenever(client.getInformatieobjecttype(
            plugin.authenticationPluginConfiguration,
            plugin.url,
            mockInformatieobjecttypeUrl2
        )).thenReturn(mockInformatieobjecttype2)

        val informatieobjecttypes = plugin.getInformatieobjecttypes(zaakTypeUrl)

        assertEquals(2, informatieobjecttypes.size)
        assertEquals(mockInformatieobjecttype1, informatieobjecttypes[0])
        assertEquals(mockInformatieobjecttype2, informatieobjecttypes[1])
    }

}
