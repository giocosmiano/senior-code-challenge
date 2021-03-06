package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.dto.CompensationDto;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public CompensationDto save(CompensationDto compensationDto) {
        LOG.debug("Creating compensation [{}]", compensationDto);

        return mapCompensationToDto(compensationRepository.save(mapDtoToCompensation(compensationDto)));
    }

    @Override
    public List<CompensationDto> readAll() {
        LOG.debug("Getting all compensations");

        return compensationRepository.findAll()
                .stream()
                .map(compensation -> mapCompensationToDto(compensation))
                .collect(Collectors.toList());
    }

    @Override
    public CompensationDto readById(String id) {
        LOG.debug("Getting compensation with compensation id [{}]", id);

        Compensation compensation = compensationRepository.findByCompensationId(id);

        if (compensation == null) {
            throw new RuntimeException("Invalid compensationId: " + id);
        }

        return mapCompensationToDto(compensation);
    }

    private Compensation mapDtoToCompensation(CompensationDto compensationDto) {
        Compensation compensation = null;

        // get by compensation id
        if (compensationDto.getCompensationId() != null) {
            compensation = compensationRepository.findByCompensationId(compensationDto.getCompensationId());
        }

        // if compensation isn't found, then see if we can find it by employee id
        if (compensation == null) {

            if (compensationDto.getEmployee() != null &&
                    compensationDto.getEmployee().getEmployeeId() != null) {

                // we need to make sure we're creating a compensation for valid employee
                Employee employee = employeeService.read(compensationDto.getEmployee().getEmployeeId());

                Compensation empCompensation = compensationRepository.findByEmployeeId(employee.getEmployeeId());
                if (empCompensation == null) {
                    compensation = new Compensation();
                    compensation.setCompensationId(UUID.randomUUID().toString());
                    compensation.setEmployeeId(employee.getEmployeeId());

                } else {
                    compensation = empCompensation;
                }

            } else {
                throw new RuntimeException("Invalid employee and/or employee id on compensation json object");
            }
        }

        // set the employee salary and effective date
        compensation.setSalary(compensationDto.getSalary());

        if (compensationDto.getEffectiveDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            LocalDate localDate = LocalDate.parse(compensationDto.getEffectiveDate(), formatter);
            compensation.setEffectiveDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (compensation.getEffectiveDate() == null) {
            throw new RuntimeException("Employee effective date is empty");
        }

        return compensation;
    }

    private CompensationDto mapCompensationToDto(Compensation compensation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        CompensationDto compensationDto = new CompensationDto();
        compensationDto.setCompensationId(compensation.getCompensationId());
        compensationDto.setEmployee(employeeService.read(compensation.getEmployeeId()));
        compensationDto.setSalary(compensation.getSalary());

        if (compensation.getEffectiveDate() != null) {
            LocalDate localDate =
                    compensation.getEffectiveDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
            compensationDto.setEffectiveDate(localDate.format(formatter));
        }

        return compensationDto;
    }
}
