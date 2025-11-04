package com.gestionpresence.service;

import com.gestionpresence.dto.EmployeeDTO;
import com.gestionpresence.exception.BusinessException;
import com.gestionpresence.exception.ResourceNotFoundException;
import com.gestionpresence.model.Employee;
import com.gestionpresence.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class EmployeeService implements IEmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByMatricule(String matricule) {
        return employeeRepository.findByMatricule(matricule);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByMatricule(employee.getMatricule())) {
            throw new BusinessException("Matricule déjà existant: " + employee.getMatricule());
        }
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new BusinessException("Email déjà existant: " + employee.getEmail());
        }

        // Générer le QR code pour l'employé
        try {
            String qrData = employee.getMatricule();
            byte[] qrCode = qrCodeService.generateQRCode(qrData, 200, 200);
            employee.setQrCode(qrCode);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR code pour {}", employee.getMatricule(), e);
            throw new BusinessException("Erreur lors de la génération du QR code");
        }

        return employeeRepository.save(employee);
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = convertToEntity(employeeDTO);
        Employee savedEmployee = createEmployee(employee);
        return convertToDTO(savedEmployee);
    }

    @Override
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'id: " + id));

        // Vérifier la contrainte d'unicité si le matricule change
        if (!employee.getMatricule().equals(employeeDetails.getMatricule()) &&
            employeeRepository.existsByMatricule(employeeDetails.getMatricule())) {
            throw new BusinessException("Matricule déjà existant: " + employeeDetails.getMatricule());
        }

        // Vérifier la contrainte d'unicité si l'email change
        if (!employee.getEmail().equals(employeeDetails.getEmail()) &&
            employeeRepository.existsByEmail(employeeDetails.getEmail())) {
            throw new BusinessException("Email déjà existant: " + employeeDetails.getEmail());
        }

        employee.setNom(employeeDetails.getNom());
        employee.setPrenom(employeeDetails.getPrenom());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPoste(employeeDetails.getPoste());
        employee.setSalaireBase(employeeDetails.getSalaireBase());

        return employeeRepository.save(employee);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee employee = convertToEntity(employeeDTO);
        Employee updatedEmployee = updateEmployee(id, employee);
        return convertToDTO(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'id: " + id));
        employeeRepository.delete(employee);
    }

    @Override
    public byte[] getEmployeeQRCode(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'id: " + id));

        if (employee.getQrCode() == null) {
            throw new BusinessException("QR code non généré pour cet employé");
        }

        return employee.getQrCode();
    }

    @Override
    public String getEmployeeQRCodeBase64(Long id) {
        byte[] qrCode = getEmployeeQRCode(id);
        return Base64.getEncoder().encodeToString(qrCode);
    }

    @Override
    public String scanQRCode(MultipartFile file) {
        try {
            // Extraire le matricule du QR code scanné
            String matricule = qrCodeService.extractDataFromQRCode(file.getBytes());

            // Vérifier que l'employé existe
            if (!employeeRepository.existsByMatricule(matricule)) {
                throw new BusinessException("Employé non trouvé avec le matricule: " + matricule);
            }

            return matricule;
        } catch (Exception e) {
            log.error("Erreur lors du scan du QR code", e);
            throw new BusinessException("Erreur lors du scan du QR code: " + e.getMessage());
        }
    }

    // DTO conversion methods
    @Override
    public EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .matricule(employee.getMatricule())
                .nom(employee.getNom())
                .prenom(employee.getPrenom())
                .email(employee.getEmail())
                .poste(employee.getPoste())
                .salaireBase(employee.getSalaireBase())
                .dateEmbauche(employee.getDateEmbauche())
                .qrCodeBase64(employee.getQrCode() != null ? Base64.getEncoder().encodeToString(employee.getQrCode()) : null)
                .build();
    }

    @Override
    public Employee convertToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setId(employeeDTO.getId());
        employee.setMatricule(employeeDTO.getMatricule());
        employee.setNom(employeeDTO.getNom());
        employee.setPrenom(employeeDTO.getPrenom());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPoste(employeeDTO.getPoste());
        employee.setSalaireBase(employeeDTO.getSalaireBase());
        employee.setDateEmbauche(employeeDTO.getDateEmbauche());
        // QR code will be generated in createEmployee method
        return employee;
    }

    @Override
    public List<EmployeeDTO> convertToDTOList(List<Employee> employees) {
        return employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> findAllAsDTO() {
        return convertToDTOList(findAll());
    }

    @Override
    public Optional<EmployeeDTO> findByIdAsDTO(Long id) {
        return findById(id).map(this::convertToDTO);
    }
}
