package com.gestionpresence.controller;

import com.gestionpresence.model.Employee;
import com.gestionpresence.service.EmployeeService;
import com.gestionpresence.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.findAll();
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        try {
            Employee savedEmployee = employeeService.createEmployee(employee);
            return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getEmployeeQRCode(@PathVariable Long id) {
        try {
            byte[] qrCode = employeeService.getEmployeeQRCode(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("filename", "qrcode.png");

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/qrcode/base64")
    public ResponseEntity<String> getEmployeeQRCodeBase64(@PathVariable Long id) {
        try {
            byte[] qrCode = employeeService.getEmployeeQRCode(id);
            String base64QRCode = java.util.Base64.getEncoder().encodeToString(qrCode);
            return ResponseEntity.ok(base64QRCode);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("QR code not found");
        }
    }

    @PostMapping("/scan")
    public ResponseEntity<String> scanQRCode(@RequestParam("file") MultipartFile file) {
        try {
            String matricule = employeeService.scanQRCode(file);
            return ResponseEntity.ok(matricule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur de scan: " + e.getMessage());
        }
    }
}
