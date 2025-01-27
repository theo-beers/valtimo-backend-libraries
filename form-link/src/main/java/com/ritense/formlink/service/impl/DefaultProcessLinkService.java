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

import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.ProcessLinkTaskProvider;
import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.ProcessLinkService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

public class DefaultProcessLinkService implements ProcessLinkService {
    private final RepositoryService repositoryService;
    private final TaskService taskService;
    private final FormAssociationService formAssociationService;
    private final List<ProcessLinkTaskProvider> processLinkTaskProviders;

    public DefaultProcessLinkService(
        RepositoryService repositoryService,
        TaskService taskService,
        FormAssociationService formAssociationService,
        List<ProcessLinkTaskProvider> processLinkTaskProviders
    ) {
        this.repositoryService = repositoryService;
        this.taskService = taskService;
        this.formAssociationService = formAssociationService;
        this.processLinkTaskProviders = processLinkTaskProviders;
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public TaskOpenResult openTask(UUID taskId) {
        Task task = taskService
            .createTaskQuery()
            .taskId(taskId.toString())
            .active()
            .singleResult();

        final var processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());

        Optional<? extends FormAssociation> formAssociationOptional = formAssociationService.getFormAssociationByFormLinkId(
            processDefinition.getKey(),
            task.getTaskDefinitionKey()
        );

        FormAssociation formAssociation = formAssociationOptional
            .orElseThrow(() -> new NoSuchElementException("Could not find FormAssociation for task " + taskId));

        FormLink formLink = formAssociation.getFormLink();
        return processLinkTaskProviders.stream()
            .filter(processLinkTaskProvider -> processLinkTaskProvider.supports(formLink))
            .map(processLinkTaskProvider -> processLinkTaskProvider.getTaskResult(task, formLink))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Could not find ProcessLinkTaskProvider for FormLink related to task " + taskId));
    }
}
