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

package com.ritense.externalevent.service

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.externalevent.messaging.ExternalDomainMessage
import com.ritense.externalevent.messaging.`in`.CompleteTaskMessage
import com.ritense.externalevent.messaging.out.CreatePortalTaskMessage
import com.ritense.externalevent.messaging.out.DeletePortalTaskMessage
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.camunda.processaudit.DeletePortalTaskEvent
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Sinks
import java.util.UUID

@Transactional
class ExternalTaskService(
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val sink: Sinks.Many<ExternalDomainMessage>
) {

    /**
     * Supplier method: Publishes the Valtimo Task back to the Portal.
     */
    fun publishPortalTask(formDefinitionName: String, task: DelegateTask) {
        val formDefinition = formIoFormDefinitionService.getFormDefinitionByName(formDefinitionName).orElseThrow()
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(task.execution.processBusinessKey))
        val document = documentService.findBy(documentId).orElseThrow()
        formDefinition.preFill(document.content().asJson())

        sink.tryEmitNext(
            CreatePortalTaskMessage(
                taskId = task.id,
                externalCaseId = document.id().id.toString(),
                formDefinition = formDefinition.asJson(),
                taskDefinitionKey = task.taskDefinitionKey
            )
        )
    }

    fun publishPortalTask(
        formDefinitionName: String,
        document: Document,
        task: DelegateTask,
        isPublic: Boolean
    ) {
        val formDefinition = formIoFormDefinitionService.getFormDefinitionByName(formDefinitionName).orElseThrow()
        formDefinition.preFill(document.content().asJson())
        sink.tryEmitNext(
            CreatePortalTaskMessage(
                taskId = task.id,
                externalCaseId = document.id().toString(),
                formDefinition = formDefinition.asJson(),
                taskDefinitionKey = task.taskDefinitionKey,
                isPublic = isPublic
            )
        )
    }

    fun completeTask(completeTaskMessage: CompleteTaskMessage) {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(completeTaskMessage.externalCaseId))
        val document = documentService.findBy(documentId).orElseThrow()

        val modifyDocumentRequest = ModifyDocumentRequest(
            completeTaskMessage.externalCaseId,
            completeTaskMessage.submission,
            document.version().toString()
        )
        val request = ModifyDocumentAndCompleteTaskRequest(modifyDocumentRequest, completeTaskMessage.taskId)

        val taskResult = processDocumentService.modifyDocumentAndCompleteTask(request)

        if (taskResult.resultingDocument().isEmpty) {
            var logMessage = "Errors occurred during completion of external task (id=${completeTaskMessage.taskId}):"
            taskResult.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
        }
    }

    /**
     * Supplier method: Tells the portal to delete a task
     */
    @EventListener(DeletePortalTaskEvent::class)
    fun deletePortalTask(event: DeletePortalTaskEvent) {
        logger.info { "Sending DeletePortalTaskMessage for taskId ${event.taskId}" }
        sink.tryEmitNext(
            DeletePortalTaskMessage(
                taskId = event.taskId
            )
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}