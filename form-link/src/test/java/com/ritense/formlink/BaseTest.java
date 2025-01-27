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

package com.ritense.formlink;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.FormSpringContextHelper;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociationId;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.impl.formassociation.FormAssociations;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import org.apache.commons.io.IOUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    protected static final String PROCESS_DEFINITION_KEY = "process-definition-key";
    protected static final String USERNAME = "test@test.com";
    protected DocumentSequenceGeneratorService documentSequenceGeneratorService;

    public BaseTest() {
        MockitoAnnotations.openMocks(this);
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
    }

    protected static void mockSpringContextHelper() {
        var applicationContext = mock(ApplicationContext.class);
        var formFieldDataResolver = mock(FormFieldDataResolver.class);
        when(formFieldDataResolver.supports(eq("oz"))).thenReturn(true);
        when(applicationContext.getBeansOfType(FormFieldDataResolver.class)).thenReturn(Map.of("Test", formFieldDataResolver));
        var springContextHelper = new FormSpringContextHelper();
        springContextHelper.setApplicationContext(applicationContext);
    }

    protected CamundaProcessFormAssociation processFormAssociation(UUID id, UUID formId) {
        return new CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.newId(id),
            PROCESS_DEFINITION_KEY,
            formAssociations(formId)
        );
    }

    protected CamundaProcessFormAssociation processFormAssociation(UUID id, UUID formAssociationsId, UUID formId) {
        return new CamundaProcessFormAssociation(
            CamundaProcessFormAssociationId.newId(id),
            PROCESS_DEFINITION_KEY,
            formAssociations(formAssociationsId, formId)
        );
    }

    private FormAssociations formAssociations(UUID formId) {
        final var formAssociations = new FormAssociations();
        formAssociations.add(
            new UserTaskFormAssociation(
                UUID.randomUUID(),
                new BpmnElementFormIdLink("user-task-id", formId)
            )
        );
        return formAssociations;
    }

    private FormAssociations formAssociations(UUID formAssociationsId, UUID formId) {
        final var formAssociations = new FormAssociations();
        formAssociations.add(
            new UserTaskFormAssociation(
                formAssociationsId,
                new BpmnElementFormIdLink("user-task-id", formId)
            )
        );
        return formAssociations;
    }

    protected CreateFormAssociationRequest createUserTaskFormAssociationRequest(UUID formId) {
        return new CreateFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            new FormLinkRequest(
                "userTaskId",
                FormAssociationType.USER_TASK,
                formId,
                null,
                null,
                null
            )
        );
    }

    protected CreateFormAssociationRequest createFormFlowUserTaskFormAssociationRequest(String formFlowId) {
        return new CreateFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            new FormLinkRequest(
                "userTaskId",
                FormAssociationType.USER_TASK,
                null,
                formFlowId,
                null,
                null
            )
        );
    }

    protected CreateFormAssociationRequest createFormAssociationRequestWithStartEvent(UUID formId) {
        return new CreateFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            new FormLinkRequest(
                "startEventId",
                FormAssociationType.START_EVENT,
                formId,
                null,
                null,
                null
            )
        );
    }

    protected ModifyFormAssociationRequest modifyFormAssociationRequest(UUID formAssociationId, UUID formId, boolean isPublic) {
        return new ModifyFormAssociationRequest(
            PROCESS_DEFINITION_KEY,
            formAssociationId,
            new FormLinkRequest(
                "userTaskId",
                FormAssociationType.USER_TASK,
                formId,
                null,
                null,
                null
            )
        );
    }

    protected CreateFormDefinitionRequest createFormDefinitionRequest() throws IOException {
        return createFormDefinitionRequest("myForm");
    }

    protected CreateFormDefinitionRequest createFormDefinitionRequest(String formName) throws IOException {
        return new CreateFormDefinitionRequest(formName, rawFormDefinition("form-example"), false);
    }

    protected String rawFormDefinition(String formDefinitionId) throws IOException {
        return IOUtils.toString(
            Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/form/" + formDefinitionId + ".json")),
            StandardCharsets.UTF_8
        );
    }

    protected FormIoFormDefinition formDefinitionOf(String formDefinitionId) throws IOException {
        final String formDefinition = rawFormDefinition(formDefinitionId);
        return new FormIoFormDefinition(UUID.randomUUID(), "form-example", formDefinition, false);
    }

    protected JsonSchemaDocumentDefinition definition() {
        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house");
        final var jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    protected JsonSchemaDocument createDocument(JsonDocumentContent content) {
        return JsonSchemaDocument
            .create(definition(), content, USERNAME, documentSequenceGeneratorService, null)
            .resultingDocument()
            .orElseThrow();
    }

    protected CamundaProcessJsonSchemaDocumentDefinition processDocumentDefinition(String documentDefinitionName) {
        return new CamundaProcessJsonSchemaDocumentDefinition(
            CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                new CamundaProcessDefinitionKey(PROCESS_DEFINITION_KEY),
                JsonSchemaDocumentDefinitionId.existingId(documentDefinitionName, 1)
            ),
            false,
            false
        );
    }

    public URI path(String name) {
        return URI.create(String.format("config/document/definition/%s.json", name + ".schema"));
    }

}