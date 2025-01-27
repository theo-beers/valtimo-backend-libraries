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

import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.service.ChoiceFieldService;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldDTO;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChoiceFieldResource {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceFieldResource.class);
    private final ChoiceFieldService choiceFieldService;

    public ChoiceFieldResource(ChoiceFieldService choiceFieldService) {
        this.choiceFieldService = choiceFieldService;
    }

    @PostMapping(value = "/v1/choice-fields")
    public ResponseEntity<ChoiceField> createChoiceField(@Valid @RequestBody ChoiceField choiceField) throws URISyntaxException {
        logger.debug("REST request to save ChoiceField : {}", choiceField);
        if (choiceField.getId() != null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert("choiceField", "idexists", "A new choiceField cannot already have an ID"))
                .body(null);
        }
        ChoiceField result = choiceFieldService.save(choiceField);
        return ResponseEntity.created(new URI("/api/v1/choice-fields/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("choiceField", result.getKeyName()))
            .body(result);
    }

    @PutMapping(value = "/v1/choice-fields")
    public ResponseEntity<ChoiceField> updateChoiceField(@Valid @RequestBody ChoiceField choiceField) throws URISyntaxException {
        logger.debug("REST request to update ChoiceField : {}", choiceField);
        if (choiceField.getId() == null) {
            return createChoiceField(choiceField);
        }
        ChoiceField result = choiceFieldService.save(choiceField);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("choiceField", choiceField.getKeyName()))
            .body(result);
    }

    @GetMapping(value = "/v1/choice-fields")
    public ResponseEntity<List<ChoiceField>> getAllChoiceFields(Pageable pageable) {
        logger.debug("REST request to get a page of ChoiceFields");
        Page<ChoiceField> page = choiceFieldService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/choice-fields");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping(value = "/v1/choice-fields/{id}")
    public ResponseEntity<ChoiceField> getChoiceField(@PathVariable Long id) {
        logger.debug("REST request to get ChoiceField : {}", id);
        Optional<ChoiceField> choiceFieldOptional = choiceFieldService.findOneById(id);
        return choiceFieldOptional
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/v1/choice-fields/name/{name}")
    public ResponseEntity<ChoiceFieldDTO> getChoiceFieldByName(@PathVariable String name) {
        logger.debug("REST request to get ChoiceField : {}", name);
        ChoiceFieldDTO choiceFieldDTO = choiceFieldService.findOneByName(name);
        return Optional.ofNullable(choiceFieldDTO)
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/v1/choice-fields/{id}")
    public ResponseEntity<Void> deleteChoiceField(@PathVariable Long id) {
        logger.debug("REST request to delete ChoiceField : {}", id);
        choiceFieldService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("choiceField", id.toString())).build();
    }

}