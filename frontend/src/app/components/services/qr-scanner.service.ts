import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QrScannerService {
  
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  scanQRCode(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post(`${this.apiUrl}/employees/scan`, formData, {
      responseType: 'text'
    });
  }

  enregistrerPresence(matricule: string, typePointage: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/presences/pointage`, null, {
      params: { matricule, typePointage }
    });
  }
}