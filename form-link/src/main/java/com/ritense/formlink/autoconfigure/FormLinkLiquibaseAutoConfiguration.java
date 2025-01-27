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

package com.ritense.formlink.autoconfigure;

import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import javax.sql.DataSource;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@ConditionalOnClass(DataSource.class)
public class FormLinkLiquibaseAutoConfiguration {

    @Order(HIGHEST_PRECEDENCE + 11)
    @Bean
    @ConditionalOnMissingBean(name = "formLinkLiquibaseMasterChangeLogLocation")
    public LiquibaseMasterChangeLogLocation formLinkLiquibaseMasterChangeLogLocation() {
        return new LiquibaseMasterChangeLogLocation("config/liquibase/formlink-master.xml");
    }

}