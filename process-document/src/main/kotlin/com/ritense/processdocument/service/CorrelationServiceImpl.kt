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

package com.ritense.processdocument.service

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.*

class CorrelationServiceImpl(
    val runtimeService: RuntimeService,
    val documentService: DocumentService,
    val camundaProcessService: CamundaProcessService,
    val repositoryService: RepositoryService,
    val associationService: ProcessDocumentAssociationService
) : CorrelationService{

    override fun sendStartMessage(message: String, businessKey: String): MessageCorrelationResult {
        return sendStartMessage(message, businessKey, null)
    }

    override fun sendStartMessage(message: String, businessKey: String, variables: Map<String, Any>?): MessageCorrelationResult {
        val result = correlate(message, businessKey,variables)
        val processName = camundaProcessService.findProcessDefinitionById(result.processInstance.processDefinitionId).name
        associateDocumentToProcess(result.processInstance.id, processName, businessKey)

        return result
    }

    override fun sendStartMessageWithProcessDefinitionKey(
        message: String,
        targetProcessDefinitionKey: String,
        businessKey: String,
        variables: Map<String, Any>?
    ){
        val processDefinitionId = getLatestProcessDefinitionIdByKey(targetProcessDefinitionKey)
        val correlationResultProcess = correlateWithProcessDefinitionId(message, businessKey, processDefinitionId.id, variables)
        val processName = camundaProcessService.findProcessDefinitionById(correlationResultProcess.processDefinitionId).name
        associateDocumentToProcess(correlationResultProcess.processInstanceId, processName, businessKey)
    }

    override fun sendCatchEventMessage(message: String, businessKey: String): MessageCorrelationResult{
        return sendCatchEventMessage(message, businessKey, null)
    }

    override fun sendCatchEventMessage(message: String, businessKey: String, variables: Map<String, Any>?): MessageCorrelationResult {
        val result = correlate(message, businessKey, variables)
        val correlationResultProcessInstance = camundaProcessService.findProcessInstanceById(result.execution.processInstanceId)
        val processName = camundaProcessService.findProcessDefinitionById(correlationResultProcessInstance.get().processDefinitionId).name
        associateDocumentToProcess(
            correlationResultProcessInstance.get().processInstanceId,
            processName,
            correlationResultProcessInstance.get().businessKey)
        return result
    }
    override fun sendCatchEventMessageToAll(message: String, businessKey: String): List<MessageCorrelationResult> {
        return sendCatchEventMessageToAll(message, businessKey, null)
    }

    override fun sendCatchEventMessageToAll(message: String, businessKey: String, variables: Map<String,Any>?): List<MessageCorrelationResult> {
        val correlationResultProcessList = correlateAll(message, businessKey, variables)
        correlationResultProcessList.forEach { correlationResultProcess ->
            val runningProcessInstance = camundaProcessService.findProcessInstanceById(correlationResultProcess.execution.processInstanceId)
            val processName = camundaProcessService.findProcessDefinitionById(runningProcessInstance.get().processDefinitionId).name
            associateDocumentToProcess(
                correlationResultProcess.execution.processInstanceId,
                processName,
                runningProcessInstance.get().businessKey)
        }
        return correlationResultProcessList
    }

    private fun getLatestProcessDefinitionIdByKey(processDefinitionKey: String): ProcessDefinition {
        return camundaProcessService.getProcessDefinition(processDefinitionKey)
            ?: throw RuntimeException("Failed to get process definition with key $processDefinitionKey")
    }

    private fun associateDocumentToProcess(
        processInstanceId: String?,
        processName: String?,
        businessKey: String
    ) {
        documentService.findBy(JsonSchemaDocumentId.existingId(UUID.fromString(businessKey)))
            .ifPresentOrElse({ document: Document ->
                associationService.createProcessDocumentInstance(
                    processInstanceId,
                    UUID.fromString(document.id().toString()),
                    processName
                )
            }) { throw DocumentNotFoundException("No Document found with id $businessKey") }
    }

    private fun correlate(
        message: String, businessKey: String, variables: Map<String, Any>?):MessageCorrelationResult{
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run {
            builder.processInstanceVariablesEqual(variables)
        }
        return builder.correlateWithResult()

    }
    private fun correlateWithProcessDefinitionId(
        message: String,
        businessKey: String,
        processDefinitionId: String,
        variables: Map<String, Any>?,
    ): ProcessInstance {
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processDefinitionId(processDefinitionId)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run {builder.setVariables(variables)}
        return builder.correlateStartMessage()
    }

    private fun correlateAll(
        message: String,
        businessKey: String,
        variables: Map<String, Any>?
    ): List<MessageCorrelationResult> {
        val builder = runtimeService.createMessageCorrelation(message)
        builder.processInstanceBusinessKey(businessKey)
        variables?.run {
            builder.processInstanceVariablesEqual(variables)
        }
        return builder.correlateAllWithResult()
    }
}