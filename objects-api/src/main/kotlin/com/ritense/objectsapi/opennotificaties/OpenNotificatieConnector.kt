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

package com.ritense.objectsapi.opennotificaties

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.objectsapi.domain.Abonnement
import com.ritense.objectsapi.domain.AbonnementLink
import com.ritense.objectsapi.domain.Kanaal
import com.ritense.objectsapi.domain.KanaalLink
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import mu.KotlinLogging
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

@ConnectorType(name = "OpenNotificatie")
class OpenNotificatieConnector(
    private var openNotificatieProperties: OpenNotificatieProperties,
    private var abonnementLinkRepository: AbonnementLinkRepository,
    private var openNotificatieClient: OpenNotificatieClient
): Connector {
    fun ensureKanaalExists() {
        if (!verifyObjectenKanaalExists()) {
            createObjectenKanaal()
        }
    }

    fun createAbonnement(connectorId: ConnectorInstanceId) {
        val key: String = createRandomKey()
        val abonnementRequest = Abonnement(
            null,
            "${openNotificatieProperties.callbackBaseUrl}/api/v1/notification?connectorId=${connectorId.id}",
            key,
            listOf(
                KanaalLink(OBJECTEN_KANAAL_NAME)
            )
        )
        val abonnement = openNotificatieClient.createAbonnement(abonnementRequest)

        val abonnementLink = AbonnementLink(
            connectorId,
            UUID.fromString(abonnement.url?.substringAfterLast('/')),
            key
        )
        abonnementLinkRepository.save(abonnementLink)
    }

    fun deleteAbonnement(connectorId: ConnectorInstanceId) {
        val abonnementLink = abonnementLinkRepository.findById(connectorId)
        abonnementLink.ifPresent {
            try {
                openNotificatieClient.deleteAbonnement(it.abonnementId)
            } catch(e: Exception) {
                // abonnement might have been deleted remotely. should not block deletion on this end
                logger.warn(e) { "abonnement could not be deleted in open notificaties" }
            }
            abonnementLinkRepository.deleteById(it.connectorId)
        }
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        openNotificatieProperties = connectorProperties as OpenNotificatieProperties
        openNotificatieClient.setProperties(openNotificatieProperties)
    }

    override fun getProperties(): OpenNotificatieProperties {
        return openNotificatieProperties
    }

    private fun verifyObjectenKanaalExists(): Boolean {
        return openNotificatieClient.getKanalen()
            .filter { it.naam.equals(OBJECTEN_KANAAL_NAME) }
            .isNotEmpty()
    }

    private fun createObjectenKanaal() {
        openNotificatieClient.createKanaal(Kanaal(OBJECTEN_KANAAL_NAME))
    }

    private fun createRandomKey(): String {
        val random = SecureRandom();
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    companion object {
        const val OBJECTEN_KANAAL_NAME = "objecten"
        val logger = KotlinLogging.logger {}
    }
}
