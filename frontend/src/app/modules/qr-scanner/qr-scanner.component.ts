import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-qr-scanner',
  templateUrl: './qr-scanner.component.html',
  styleUrls: ['./qr-scanner.component.css']
})
export class QrScannerComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement', { static: true }) videoElement!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement', { static: true }) canvasElement!: ElementRef<HTMLCanvasElement>;

  isScanning = false;
  isProcessing = false;
  stream: MediaStream | null = null;
  scanResult = '';
  employeeInfo: any = null;

  private apiUrl = 'http://localhost:8080/api/presences';
  private scanInterval: any;

  constructor(private snackBar: MatSnackBar, private http: HttpClient) { }

  ngOnInit(): void {
    // Check if camera is available
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      this.showMessage('La caméra n\'est pas disponible sur ce navigateur', 'error');
    }
  }

  ngOnDestroy(): void {
    this.stopScanning();
  }

  async startScanning(): Promise<void> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' } // Use back camera on mobile
      });

      this.videoElement.nativeElement.srcObject = this.stream;
      this.isScanning = true;

      // Start scanning for QR codes
      this.scanInterval = setInterval(() => {
        this.scanQRCode();
      }, 500); // Scan every 500ms

      this.showMessage('Scan en cours...', 'info');
    } catch (error) {
      console.error('Erreur lors de l\'accès à la caméra:', error);
      this.showMessage('Impossible d\'accéder à la caméra', 'error');
    }
  }

  stopScanning(): void {
    this.isScanning = false;

    if (this.scanInterval) {
      clearInterval(this.scanInterval);
    }

    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }

    this.videoElement.nativeElement.srcObject = null;
    this.scanResult = '';
    this.employeeInfo = null;
  }

  private scanQRCode(): void {
    if (this.isProcessing) return;

    const video = this.videoElement.nativeElement;
    const canvas = this.canvasElement.nativeElement;
    const context = canvas.getContext('2d');

    if (!context || video.videoWidth === 0 || video.videoHeight === 0) return;

    // Draw current video frame to canvas
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    // Get image data for QR code processing
    const imageData = context.getImageData(0, 0, canvas.width, canvas.height);

    // Here you would integrate with a QR code library like jsQR
    // For now, we'll simulate QR code detection
    this.simulateQRCodeDetection();
  }

  private simulateQRCodeDetection(): void {
    // Simulate QR code detection (replace with actual QR library)
    if (Math.random() < 0.05) { // 5% chance every scan
      const mockEmployeeId = Math.floor(Math.random() * 10) + 1;
      this.processQRCode(`EMP${mockEmployeeId.toString().padStart(3, '0')}`);
    }
  }

  private processQRCode(qrData: string): void {
    if (this.isProcessing) return;

    this.isProcessing = true;
    this.scanResult = qrData;

    // Extract employee ID from QR code
    const employeeId = this.extractEmployeeId(qrData);

    if (employeeId) {
      this.recordPresence(employeeId);
    } else {
      this.showMessage('Code QR invalide', 'error');
      this.isProcessing = false;
    }
  }

  private extractEmployeeId(qrData: string): number | null {
    // Extract employee ID from QR code data
    // Assuming QR code contains employee matricule like "EMP001"
    const match = qrData.match(/EMP(\d+)/);
    return match ? parseInt(match[1]) : null;
  }

  private recordPresence(employeeId: number): void {
    const presenceData = {
      employeeId: employeeId,
      datePresence: new Date().toISOString().split('T')[0],
      heureEntree: new Date().toTimeString().split(' ')[0].substring(0, 5),
      statut: 'PRESENT'
    };

    this.http.post(`${this.apiUrl}`, presenceData).subscribe({
      next: (response: any) => {
        this.employeeInfo = response.employee;
        this.showMessage(`Pointage enregistré pour ${response.employee.prenom} ${response.employee.nom}`, 'success');
        this.isProcessing = false;

        // Auto-stop scanning after successful record
        setTimeout(() => {
          this.stopScanning();
        }, 3000);
      },
      error: (error) => {
        console.error('Erreur lors de l\'enregistrement:', error);
        this.showMessage('Erreur lors de l\'enregistrement du pointage', 'error');
        this.isProcessing = false;
      }
    });
  }

  private showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: type === 'success' ? 'snackbar-success' : type === 'error' ? 'snackbar-error' : 'snackbar-info'
    });
  }

  // Manual entry for testing
  manualEntry(employeeId: string): void {
    if (employeeId) {
      const id = parseInt(employeeId);
      if (!isNaN(id)) {
        this.processQRCode(`EMP${id.toString().padStart(3, '0')}`);
      }
    }
  }
}
