import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Employee {
  id?: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  poste: string;
  salaireBase: number;
  dateEmbauche?: string;
}

@Component({
  selector: 'app-employees',
  templateUrl: './employees.component.html',
  styleUrls: ['./employees.component.css']
})
export class EmployeesComponent implements OnInit {
  employees: Employee[] = [];
  employeeForm: FormGroup;
  isEditing = false;
  selectedEmployee: Employee | null = null;
  showForm = false;

  private apiUrl = 'http://localhost:8080/api/employees';

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.employeeForm = this.fb.group({
      matricule: ['', [Validators.required, Validators.minLength(3)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      poste: ['', [Validators.required]],
      salaireBase: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.http.get<Employee[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.employees = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des employés:', error);
        // Mock data for demonstration
        this.employees = [
          {
            id: 1,
            matricule: 'EMP001',
            nom: 'Dupont',
            prenom: 'Jean',
            email: 'jean.dupont@email.com',
            poste: 'Développeur',
            salaireBase: 2500,
            dateEmbauche: '2023-01-15'
          },
          {
            id: 2,
            matricule: 'EMP002',
            nom: 'Martin',
            prenom: 'Marie',
            email: 'marie.martin@email.com',
            poste: 'Designer',
            salaireBase: 2300,
            dateEmbauche: '2023-02-01'
          }
        ];
      }
    });
  }

  showAddForm(): void {
    this.isEditing = false;
    this.selectedEmployee = null;
    this.employeeForm.reset();
    this.showForm = true;
  }

  editEmployee(employee: Employee): void {
    this.isEditing = true;
    this.selectedEmployee = employee;
    this.employeeForm.patchValue(employee);
    this.showForm = true;
  }

  cancelEdit(): void {
    this.showForm = false;
    this.employeeForm.reset();
    this.isEditing = false;
    this.selectedEmployee = null;
  }

  saveEmployee(): void {
    if (this.employeeForm.valid) {
      const employeeData = this.employeeForm.value;

      if (this.isEditing && this.selectedEmployee) {
        // Update existing employee
        this.http.put<Employee>(`${this.apiUrl}/${this.selectedEmployee.id}`, employeeData).subscribe({
          next: (updatedEmployee) => {
            const index = this.employees.findIndex(e => e.id === updatedEmployee.id);
            if (index !== -1) {
              this.employees[index] = updatedEmployee;
            }
            this.cancelEdit();
          },
          error: (error) => {
            console.error('Erreur lors de la mise à jour:', error);
            // Mock update for demonstration
            if (this.selectedEmployee) {
              const index = this.employees.findIndex(e => e.id === this.selectedEmployee!.id);
              if (index !== -1) {
                this.employees[index] = { ...this.selectedEmployee, ...employeeData };
              }
            }
            this.cancelEdit();
          }
        });
      } else {
        // Create new employee
        this.http.post<Employee>(this.apiUrl, employeeData).subscribe({
          next: (newEmployee) => {
            this.employees.push(newEmployee);
            this.cancelEdit();
          },
          error: (error) => {
            console.error('Erreur lors de la création:', error);
            // Mock creation for demonstration
            const newEmployee: Employee = {
              id: Math.max(...this.employees.map(e => e.id || 0)) + 1,
              ...employeeData,
              dateEmbauche: new Date().toISOString().split('T')[0]
            };
            this.employees.push(newEmployee);
            this.cancelEdit();
          }
        });
      }
    }
  }

  deleteEmployee(employee: Employee): void {
    if (employee.id && confirm(`Êtes-vous sûr de vouloir supprimer ${employee.prenom} ${employee.nom} ?`)) {
      this.http.delete(`${this.apiUrl}/${employee.id}`).subscribe({
        next: () => {
          this.employees = this.employees.filter(e => e.id !== employee.id);
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          // Mock deletion for demonstration
          this.employees = this.employees.filter(e => e.id !== employee.id);
        }
      });
    }
  }

  generateQRCode(employee: Employee): void {
    if (employee.id) {
      // This would typically open a new window or download the QR code
      window.open(`${this.apiUrl}/${employee.id}/qrcode`, '_blank');
    }
  }

  copyQRCode(employee: Employee): void {
    if (employee.id) {
      this.http.get(`${this.apiUrl}/${employee.id}/qrcode/base64`, { responseType: 'text' })
        .subscribe({
          next: (base64Data: string) => {
            // Create a temporary image element to copy to clipboard
            const img = new Image();
            img.src = 'data:image/png;base64,' + base64Data;
            img.onload = () => {
              const canvas = document.createElement('canvas');
              canvas.width = img.width;
              canvas.height = img.height;
              const ctx = canvas.getContext('2d');
              if (ctx) {
                ctx.drawImage(img, 0, 0);
                canvas.toBlob((blob) => {
                  if (blob) {
                    navigator.clipboard.write([
                      new ClipboardItem({ 'image/png': blob })
                    ]).then(() => {
                      alert('QR Code copié dans le presse-papiers!');
                    }).catch((err) => {
                      console.error('Erreur lors de la copie:', err);
                      alert('Erreur lors de la copie du QR Code');
                    });
                  }
                });
              }
            };
          },
          error: (error) => {
            console.error('Erreur lors de la récupération du QR code:', error);
            alert('Erreur lors de la récupération du QR Code');
          }
        });
    }
  }
}
