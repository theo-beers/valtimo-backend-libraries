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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.Document;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.impl.SearchCriteria;
import com.ritense.document.web.rest.DocumentSearchResource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class JsonSchemaDocumentSearchResource implements DocumentSearchResource {

    private final DocumentSearchService documentSearchService;

    @Override
    @PostMapping(value = "/document-search/{document-definition-name}")
    public ResponseEntity<Page<? extends Document>> search(
        @PathVariable(name = "document-definition-name") String documentDefinitionName,
        @RequestBody List<SearchCriteria> searchCriteria,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            documentSearchService.search(documentDefinitionName, searchCriteria, pageable)
        );
    }

    @Override
    @GetMapping(value = "/document-search")
    public ResponseEntity<Page<? extends Document>> search(
        @RequestParam(name = "definitionName", required = false) String documentDefinitionName,
        @RequestParam(name = "searchCriteria", required = false) String searchCriteria,
        @RequestParam(name = "sequence", required = false) Long sequence,
        @RequestParam(name = "createdBy", required = false) String createdBy,
        @PageableDefault(sort = {"createdOn"}, direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            documentSearchService.search(
                documentDefinitionName,
                searchCriteria,
                sequence,
                createdBy,
                pageable
            )
        );
    }

}