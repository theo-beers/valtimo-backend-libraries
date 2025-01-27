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

package com.ritense.plugin.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class PluginHttpSecurityConfigurer: HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/v1/plugin/definition").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/v1/plugin/configuration").hasAuthority(ADMIN)
                .antMatchers(POST, "/api/v1/plugin/configuration").hasAuthority(ADMIN)
                .antMatchers(PUT, "/api/v1/plugin/configuration/{pluginConfigurationId}").hasAuthority(ADMIN)
                .antMatchers(DELETE, "/api/v1/plugin/configuration/{pluginConfigurationId}").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/v1/plugin/definition/{pluginDefinitionKey}/action").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/v1/process-link").hasAuthority(ADMIN)
                .antMatchers(POST, "/api/v1/process-link").hasAuthority(ADMIN)
                .antMatchers(PUT, "/api/v1/process-link").hasAuthority(ADMIN)
                .antMatchers(DELETE, "/api/v1/process-link/{processLinkId}").hasAuthority(ADMIN)
        } catch(e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
