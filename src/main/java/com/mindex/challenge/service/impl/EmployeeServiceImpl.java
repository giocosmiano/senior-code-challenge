package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    public ReportingStructure getEmployeeNumberOfReports(String id) {
        ReportingStructure rs = new ReportingStructure();
        Employee employee = employeeRepository.findByEmployeeId(id);
        rs.setEmployee(employee);

        List<Employee> listOfSubordinates = collectEmployeeSubordinates(employee);
        rs.setNumberOfReports(listOfSubordinates.size());
        return rs;
    }

    /*
     * NOTE: This function is making an assumption that employee/subordinates relationships are establish *correctly*
     * otherwise it will return an empty list since Java doesn't have a `Bottom` type, see for details --> https://en.wikipedia.org/wiki/Bottom_type
     */
    private List<Employee> collectEmployeeSubordinates(Employee employee) {
        List<Employee> subordinates = new ArrayList<>();

        if (employee.getDirectReports() != null) {

            subordinates = employee.getDirectReports()
                    .stream()
                    .map(directReport -> {
                        List<Employee> listOfSubordinates = new ArrayList<>();
                        Employee subordinate = employeeRepository.findByEmployeeId(directReport.getEmployeeId());
                        if (subordinate != null) {
                            LOG.info("processing employee [{}]; subordinate [{}]",
                                    employee.getFirstName() + " " + employee.getLastName(),
                                    subordinate.getFirstName() + " " + subordinate.getLastName());
                            listOfSubordinates.add(subordinate);
                            listOfSubordinates.addAll(collectEmployeeSubordinates(subordinate));
                        }
                        return listOfSubordinates;
                    })
                    .flatMap(x -> x.stream())
                    .collect(Collectors.toList());
        }

        return subordinates;
    }
}
