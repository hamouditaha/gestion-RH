package com.gestionpresence.controller;

import com.gestionpresence.dto.EmployeeDTO;
import com.gestionpresence.model.Employee;
import com.gestionpresence.service.EmployeeService;
import com.gestionpresence.service.QRCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        log.info("Récupération de tous les employés");
        List<EmployeeDTO> employees = employeeService.findAllAsDTO();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable @NotNull Long id) {
        log.info("Récupération de l'employé avec l'id: {}", id);
        EmployeeDTO employee = employeeService.findByIdAsDTO(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("Création d'un nouvel employé: {}", employeeDTO.getMatricule());
        EmployeeDTO savedEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("Mise à jour de l'employé avec l'id: {}", id);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable @NotNull Long id) {
        log.info("Suppression de l'employé avec l'id: {}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getEmployeeQRCode(@PathVariable @NotNull Long id) {
        log.info("Récupération du QR code pour l'employé: {}", id);
        byte[] qrCode = employeeService.getEmployeeQRCode(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("filename", "qrcode-" + id + ".png");

        return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/qrcode/base64")
    public ResponseEntity<String> getEmployeeQRCodeBase64(@PathVariable @NotNull Long id) {
        log.info("Récupération du QR code en base64 pour l'employé: {}", id);
        String base64QRCode = employeeService.getEmployeeQRCodeBase64(id);
        return ResponseEntity.ok(base64QRCode);
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanQRCode(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("Scan du QR code depuis le fichier: {}", file.getOriginalFilename());
        String matricule = employeeService.scanQRCode(file);
        return ResponseEntity.ok(matricule);
    }
}
