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

package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import java.util.function.Function
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.core.annotation.Order

/**
 * This resolver returns the requestedValue as the value.
 * It will do a best-effort of guessing the type of the given requestedValue before returning it.
 *
 * For instance, "true" will become the boolean <code>true</code>
 *
 * These requestedValues do not have a prefix
 */
class FixedValueResolverFactory : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return ""
    }

    override fun createResolver(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return Function { requestedValue->
            requestedValue.toBooleanStrictOrNull()
                ?: requestedValue.toLongOrNull()
                ?: requestedValue.toDoubleOrNull()
                ?: requestedValue
        }
    }
}