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

package com.ritense.valtimo.web.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CoreSecuritySmokeIntegrationTest extends SecuritySmokeIntegrationTest {

    @Override
    protected Set<String> getIgnoredPathPatterns() {
        return new HashSet<>(
            Arrays.asList(
                "/api/v1/public/process/definition/{processDefinitionKey}/start-form",
                "/api/v1/ping",
                "/api/v1/mandrill/webhook"
            )
        );
    }

}