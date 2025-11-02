package com.gestionpresence.service;

import com.gestionpresence.model.Employee;
import com.gestionpresence.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class EmployeeService {

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

    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByMatricule(employee.getMatricule())) {
            throw new RuntimeException("Matricule déjà existant: " + employee.getMatricule());
        }
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Email déjà existant: " + employee.getEmail());
        }

        // Générer le QR code pour l'employé
        try {
            String qrData = employee.getMatricule();
            byte[] qrCode = qrCodeService.generateQRCode(qrData, 200, 200);
            employee.setQrCode(qrCode);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR code pour {}", employee.getMatricule(), e);
            throw new RuntimeException("Erreur lors de la génération du QR code");
        }

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'id: " + id));

        // Vérifier la contrainte d'unicité si le matricule change
        if (!employee.getMatricule().equals(employeeDetails.getMatricule()) &&
            employeeRepository.existsByMatricule(employeeDetails.getMatricule())) {
            throw new RuntimeException("Matricule déjà existant: " + employeeDetails.getMatricule());
        }

        // Vérifier la contrainte d'unicité si l'email change
        if (!employee.getEmail().equals(employeeDetails.getEmail()) &&
            employeeRepository.existsByEmail(employeeDetails.getEmail())) {
            throw new RuntimeException("Email déjà existant: " + employeeDetails.getEmail());
        }

        employee.setNom(employeeDetails.getNom());
        employee.setPrenom(employeeDetails.getPrenom());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPoste(employeeDetails.getPoste());
        employee.setSalaireBase(employeeDetails.getSalaireBase());

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'id: " + id));
        employeeRepository.delete(employee);
    }

    public byte[] getEmployeeQRCode(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'id: " + id));

        if (employee.getQrCode() == null) {
            throw new RuntimeException("QR code non généré pour cet employé");
        }

        return employee.getQrCode();
    }

    public String scanQRCode(MultipartFile file) {
        try {
            // Extraire le matricule du QR code scanné
            String matricule = qrCodeService.extractDataFromQRCode(file.getBytes());

            // Vérifier que l'employé existe
            if (!employeeRepository.existsByMatricule(matricule)) {
                throw new RuntimeException("Employé non trouvé avec le matricule: " + matricule);
            }

            return matricule;
        } catch (Exception e) {
            log.error("Erreur lors du scan du QR code", e);
            throw new RuntimeException("Erreur lors du scan du QR code: " + e.getMessage());
        }
    }
}
