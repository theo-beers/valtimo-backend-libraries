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

package com.ritense.formlink.web.rest.impl;

import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.domain.impl.formassociation.FormTaskOpenResultProperties;
import com.ritense.formlink.web.rest.ProcessLinkResource;
import java.util.UUID;
import org.camunda.bpm.engine.TaskService;
import org.springframework.http.ResponseEntity;

public class MockProcessLinkResource implements ProcessLinkResource {
    private final TaskService taskService;

    public MockProcessLinkResource(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public ResponseEntity<TaskOpenResult> getTask(UUID taskId) {
        return ResponseEntity.ok(
            new TaskOpenResult(
                "form",
                new FormTaskOpenResultProperties(
                    taskService.createTaskQuery()
                        .taskId(taskId.toString())
                        .singleResult()
                        .getTaskDefinitionKey()
                )
            )
        );
    }
}