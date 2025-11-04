package com.gestionpresence.service;

import com.gestionpresence.dto.EmployeeDTO;
import com.gestionpresence.model.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IEmployeeService {

    List<Employee> findAll();

    List<EmployeeDTO> findAllAsDTO();

    Optional<Employee> findById(Long id);

    Optional<EmployeeDTO> findByIdAsDTO(Long id);

    Optional<Employee> findByMatricule(String matricule);

    Employee createEmployee(Employee employee);

    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    Employee updateEmployee(Long id, Employee employeeDetails);

    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);

    void deleteEmployee(Long id);

    byte[] getEmployeeQRCode(Long id);

    String getEmployeeQRCodeBase64(Long id);

    String scanQRCode(MultipartFile file);

    // Conversion methods
    EmployeeDTO convertToDTO(Employee employee);

    Employee convertToEntity(EmployeeDTO employeeDTO);

    List<EmployeeDTO> convertToDTOList(List<Employee> employees);
}
