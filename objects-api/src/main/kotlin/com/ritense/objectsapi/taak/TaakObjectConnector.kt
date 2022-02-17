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

package com.ritense.objectsapi.taak

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.Record
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.service.ObjectsApiConnector
import com.ritense.objectsapi.taak.TaakObjectConnector.Companion.TAAK_CONNECTOR_NAME
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.openzaak.provider.KvkProvider
import com.ritense.objectsapi.taak.resolve.ValueResolverService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.contract.json.Mapper
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
@ConnectorType(name = TAAK_CONNECTOR_NAME)
class TaakObjectConnector(
    private var taakProperties: TaakProperties,
    private val valueResolverService: ValueResolverService,
    private val connectorService: ConnectorService,
    private val bsnProvider: BsnProvider?,
    private val kvkProvider: KvkProvider?
):Connector {

    init {
        require(bsnProvider != null || kvkProvider != null) { "BSN and/or KvK provider is required!"}
    }

    fun createTask(task: DelegateTask, formulierId: String) {
        val taakObject = createTaakObjectDto(task, formulierId)

        createObjectRecord(taakObject)
    }

    fun getTaakObject(productAanvraagId: UUID): TaakObjectDto {
        val type = ObjectsApiConnector.typeReference<GenericObject<TaakObjectDto>>()
        return getObjectsApiConnector().getTypedObject(productAanvraagId, type).record.data
    }

    fun deleteTaakObject(productAanvraagId: UUID) {
        getObjectsApiConnector().deleteObject(productAanvraagId)
    }

    private fun createObjectRecord(taakObject: TaakObjectDto) {
        val objectsApiConnector = getObjectsApiConnector()
        val objectType = objectsApiConnector.getProperties().objectType
        objectsApiConnector.createObject(
            CreateObjectRequest(
                type = URI(objectType.url),
                Record(
                    typeVersion = objectType.typeVersion,
                    startAt = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()),
                    data = Mapper.INSTANCE.get().convertValue(taakObject, jacksonTypeRef<Map<String, Any>>())
                )
            )
        )
    }

    private fun createTaakObjectDto(
        task: DelegateTask,
        formulierId: String
    ): TaakObjectDto {
        val taakObject = TaakObjectDto(
            bsn = bsnProvider?.getBurgerServiceNummer(task),
            kvk = kvkProvider?.getKvkNummer(task),
            verwerkerTaakId = UUID.fromString(task.id),
            formulierId = formulierId,
            data = getTaskProperties(task),
            status = TaakObjectStatus.open
        )
        return taakObject
    }

    private fun getTaskProperties(task: DelegateTask): Map<String, Any> {
        val taakProperties = task.bpmnModelElementInstance.extensionElements.elements
            .filterIsInstance<CamundaProperties>()
            .single()
            .camundaProperties
            .filter { it.camundaName.startsWith(prefix = "taak:", ignoreCase = true) }

        val resolvedValues = valueResolverService.resolveValues(
            processInstanceId = CamundaProcessInstanceId(task.processInstanceId),
            variableScope = task,
            taakProperties.map { it.camundaValue }
        )

        // This is a workaround for Kotlin not having an associateNotNull method
        return taakProperties.mapNotNull { property ->
            resolvedValues[property.camundaValue]?.let { value ->
                property.camundaName.substringAfter(delimiter = ":") to value
            }
        }.toMap()
    }

    private fun getObjectsApiConnector(): ObjectsApiConnector {
        return connectorService.loadByName(taakProperties.objectsApiConnectionName) as ObjectsApiConnector
    }

    private fun getOpenNotificatieConnector(): OpenNotificatieConnector {
        return connectorService.loadByName(taakProperties.openNotificatieConnectionName) as OpenNotificatieConnector
    }

    override fun onCreate(connectorInstance: ConnectorInstance) {
        val openNotificatieConnector = getOpenNotificatieConnector()
        openNotificatieConnector.ensureKanaalExists()
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onEdit(connectorInstance: ConnectorInstance) {
        val openNotificatieConnector = getOpenNotificatieConnector()
        openNotificatieConnector.deleteAbonnement(connectorInstance.id)
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onDelete(connectorInstance: ConnectorInstance) {
        val openNotificatieConnector = getOpenNotificatieConnector()
        openNotificatieConnector.deleteAbonnement(connectorInstance.id)
    }

    override fun getProperties(): ConnectorProperties {
        return taakProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        taakProperties = connectorProperties as TaakProperties
    }

    companion object {
        const val TAAK_CONNECTOR_NAME = "Taak"
    }
}