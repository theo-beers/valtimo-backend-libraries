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

package com.ritense.formlink.service.impl;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Transactional
public class CamundaFormAssociationServiceIntTest extends BaseIntegrationTest {

    @Inject
    public CamundaFormAssociationService formAssociationService;

    @Inject
    public FormIoFormDefinitionService formDefinitionService;

    private FormDefinition formDefinition;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() throws IOException {
        jdbcTemplate.execute("DELETE FROM process_form_association_v2");
        jdbcTemplate.execute("DELETE FROM form_io_form_definition");
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest());
    }

    @Test
    public void shouldCreateFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var formAssociationSaved = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService.getFormAssociationById(
            createFormAssociationRequest.getProcessDefinitionKey(),
            formAssociationSaved.getId()
        ).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldNotDuplicateFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        formAssociationService.createFormAssociation(createFormAssociationRequest);
        assertThrows(DuplicateKeyException.class, () -> formAssociationService.createFormAssociation(createFormAssociationRequest));
    }

    @Test
    public void shouldGetUserTaskFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, savedFormAssociation.getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation).isInstanceOfAny(UserTaskFormAssociation.class);

        final var userTaskFormAssociation = (UserTaskFormAssociation) formAssociation;
        assertThat(userTaskFormAssociation.getId()).isEqualTo(savedFormAssociation.getId());
        assertThat(userTaskFormAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(userTaskFormAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldGetStartEventFormAssociation() {
        final var request = createFormAssociationRequestWithStartEvent(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(request);

        final var formAssociation = formAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, savedFormAssociation.getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation).isInstanceOfAny(StartEventFormAssociation.class);

        final var startEventFormAssociation = (StartEventFormAssociation) formAssociation;
        assertThat(startEventFormAssociation.getId()).isEqualTo(savedFormAssociation.getId());
        assertThat(startEventFormAssociation.getFormLink().getFormId()).isEqualTo(request.getFormLinkRequest().getFormId());
        assertThat(startEventFormAssociation.getFormLink().getId()).isEqualTo(request.getFormLinkRequest().getId());
    }

    @Test
    public void shouldModifyFormAssociation() throws IOException {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var secondFormDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest("myOtherForm"));
        final var modifyFormAssociationRequest = modifyFormAssociationRequest(savedFormAssociation.getId(), secondFormDefinition.getId(), true);

        formAssociationService.modifyFormAssociation(modifyFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, modifyFormAssociationRequest.getFormLinkRequest().getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getFormId()).isNotEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
    }

    @Test
    public void shouldGetStartEventFormDefinition() {
        final var request = createFormAssociationRequestWithStartEvent(formDefinition.getId());

        formAssociationService.createFormAssociation(request);

        final var formDefinition = formAssociationService
            .getStartEventFormDefinition(PROCESS_DEFINITION_KEY).orElseThrow();

        assertThat(formDefinition).isNotNull();
        assertThat(formDefinition.get("formAssociation")).isNotNull();
    }

    @Test
    public void shouldCreateUserFormAssociation() {
        final var request = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(
            request.getProcessDefinitionKey(),
            "myForm",
            request.getFormLinkRequest().getId(),
            request.getFormLinkRequest().getType()
        );

        final var formDefinition = formAssociationService
            .getFormDefinitionByFormLinkId(PROCESS_DEFINITION_KEY, request.getFormLinkRequest().getId())
            .orElseThrow();

        assertThat(formDefinition).isNotNull();
        assertThat(formDefinition.get("formAssociation")).isNotNull();
    }

    @Test
    public void shouldGetPresentOptionalWhenFormAssociationByFormLinkId() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());
        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, savedFormAssociation.getFormLink().getId());

        assertThat(formAssociation).isPresent();
    }

    @Test
    public void shouldGetEmptyOptionalWhenFormAssociationByFormLinkId() {
        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, "uknownId");

        assertThat(formAssociation).isEmpty();
    }

    @Test
    public void shouldGetAllFormAssociations() {
        final var userTaskFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());
        formAssociationService.createFormAssociation(
            userTaskFormAssociationRequest
        );

        final var formAssociations = formAssociationService
            .getAllFormAssociations(PROCESS_DEFINITION_KEY);

        assertThat(formAssociations).hasSize(1);
    }

    @Test
    public void shouldGetZeroFormAssociations() {
        final var formAssociations = formAssociationService
            .getAllFormAssociations(PROCESS_DEFINITION_KEY);
        assertThat(formAssociations).isNull();
    }

    @Test
    public void shouldCreateFormFlowUserFormAssociation() {
        final var formFlowId = "myFormFlowId";
        final var request = createFormFlowUserTaskFormAssociationRequest(formFlowId);

        final var savedFormAssociation = formAssociationService.createFormAssociation(request);

        final var formDefinition = formAssociationService
            .getFormDefinitionByFormLinkId(PROCESS_DEFINITION_KEY, request.getFormLinkRequest().getId())
            .orElseThrow();

        assertThat(formDefinition).isNotNull();
        assertThat(formDefinition.get("formAssociation")).isNotNull();
    }

}
