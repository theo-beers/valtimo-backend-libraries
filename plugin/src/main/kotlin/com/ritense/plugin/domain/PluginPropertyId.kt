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

package com.ritense.plugin.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ritense.valtimo.contract.domain.AbstractId
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne

@Embeddable
class PluginPropertyId(
    @Column(name = "plugin_property_key")
    val key: String,
    @JsonIgnore
    @ManyToOne(targetEntity = PluginDefinition::class, fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "plugin_definition_key", referencedColumnName = "plugin_definition_key"),
    )
    var pluginDefinition: PluginDefinition
): AbstractId<PluginPropertyId>()