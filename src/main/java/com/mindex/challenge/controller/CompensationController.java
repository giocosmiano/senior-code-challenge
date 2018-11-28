package com.mindex.challenge.controller;

import com.mindex.challenge.dto.CompensationDto;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    /**
     * using PostMan
     * POST to --> http://localhost:8080/compensation/
     * type: application/json
     * body:
     * {
     *  "employee" : {
     *      "employeeId" : "16a596ae-edd3-4847-99fe-c4518e82c86f"
     *  },
     * 	"salary" : "1000",
     * 	"effectiveDate" : "2018-11-26"
     * }
     */
    @PostMapping("/compensation")
    public CompensationDto createUpdate(@RequestBody CompensationDto compensationDto) {
        LOG.debug("Received compensation create/update request for [{}]", compensationDto);
        return compensationService.save(compensationDto);
    }

    @GetMapping("/compensation")
    public List<CompensationDto> readAll() {
        LOG.debug("Getting all compensations");
        return compensationService.readAll();
    }

    @GetMapping("/compensation/{id}")
    public CompensationDto readById(@PathVariable String id) {
        LOG.debug("Received compensation create request for compensation id [{}]", id);
        return compensationService.readById(id);
    }
}
