import { Component } from '@angular/core';
import { QrScannerService } from '../services/qr-scanner.service';

@Component({
  selector: 'app-qr-scanner',
  template: `
    <div class="scanner-container">
      <h3>Scanner QR Code Employé</h3>
      
      <input type="file" (change)="onFileSelected($event)" accept="image/*" #fileInput>
      
      <div class="actions">
        <button (click)="enregistrerEntree()" [disabled]="!currentMatricule">
          📥 Entrée
        </button>
        <button (click)="enregistrerSortie()" [disabled]="!currentMatricule">
          📤 Sortie
        </button>
      </div>
      
      <div *ngIf="message" class="message" [class.success]="isSuccess" [class.error]="!isSuccess">
        {{ message }}
      </div>
    </div>
  `,
  styles: [`
    .scanner-container {
      text-align: center;
      padding: 20px;
    }
    .actions {
      margin: 20px 0;
    }
    button {
      margin: 0 10px;
      padding: 10px 20px;
      font-size: 16px;
    }
    .message {
      margin-top: 20px;
      padding: 10px;
      border-radius: 5px;
    }
    .success {
      background-color: #d4edda;
      color: #155724;
    }
    .error {
      background-color: #f8d7da;
      color: #721c24;
    }
  `]
})
export class QrScannerComponent {
  currentMatricule: string = '';
  message: string = '';
  isSuccess: boolean = false;

  constructor(private qrService: QrScannerService) {}

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.qrService.scanQRCode(file).subscribe({
        next: (matricule) => {
          this.currentMatricule = matricule;
          this.showMessage(`Employé ${matricule} détecté`, true);
        },
        error: (error) => {
          this.showMessage('Erreur de scan: ' + error.error, false);
        }
      });
    }
  }

  enregistrerEntree(): void {
    this.enregistrerPresence('ENTREE');
  }

  enregistrerSortie(): void {
    this.enregistrerPresence('SORTIE');
  }

  private enregistrerPresence(type: string): void {
    this.qrService.enregistrerPresence(this.currentMatricule, type).subscribe({
      next: () => {
        this.showMessage(`Pointage ${type.toLowerCase()} enregistré avec succès`, true);
        this.currentMatricule = '';
      },
      error: (error) => {
        this.showMessage('Erreur: ' + error.error, false);
      }
    });
  }

  private showMessage(msg: string, success: boolean): void {
    this.message = msg;
    this.isSuccess = success;
    setTimeout(() => this.message = '', 3000);
  }
}