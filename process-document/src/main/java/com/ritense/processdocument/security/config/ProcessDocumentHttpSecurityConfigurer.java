/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.security.config;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class ProcessDocumentHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/process-document/definition").hasAuthority(USER)
                .antMatchers(POST, "/api/process-document/definition").hasAuthority(ADMIN)
                .antMatchers(DELETE, "/api/process-document/definition").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/process-document/definition/document/{document-definition-name}").hasAuthority(USER)
                .antMatchers(GET, "/api/process-document/instance/document/{document-id}").hasAuthority(USER)
                .antMatchers(GET, "/api/process-document/instance/document/{document-id}/audit").hasAuthority(USER)
                .antMatchers(POST, "/api/process-document/operation/new-document-and-start-process").hasAuthority(USER)
                .antMatchers(POST, "/api/process-document/operation/modify-document-and-complete-task").hasAuthority(USER)
                .antMatchers(POST, "/api/process-document/operation/modify-document-and-start-process").hasAuthority(USER);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

}