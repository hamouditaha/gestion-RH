import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { HttpClient } from '@angular/common/http';

interface Presence {
  id?: number;
  employeeId: number;
  employeeName: string;
  datePresence: string;
  heureEntree?: string;
  heureSortie?: string;
  statut: 'PRESENT' | 'ABSENT' | 'RETARD' | 'DEMI_JOURNEE';
  motifAbsence?: string;
}

@Component({
  selector: 'app-presences',
  templateUrl: './presences.component.html',
  styleUrls: ['./presences.component.css']
})
export class PresencesComponent implements OnInit {
  displayedColumns: string[] = ['employeeName', 'datePresence', 'heureEntree', 'heureSortie', 'statut', 'motifAbsence'];
  dataSource = new MatTableDataSource<Presence>();
  presences: Presence[] = [];
  isLoading = false;

  // Statistics
  stats = {
    totalPresences: 0,
    presentsToday: 0,
    absentsToday: 0,
    retardsToday: 0
  };

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private apiUrl = 'http://localhost:8080/api/presences';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.loadPresences();
    this.loadStats();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadPresences(): void {
    this.isLoading = true;
    this.http.get<Presence[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.presences = data;
        this.dataSource.data = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des présences:', error);
        // Mock data for demonstration
        this.presences = [
          {
            id: 1,
            employeeId: 1,
            employeeName: 'Jean Dupont',
            datePresence: '2024-01-15',
            heureEntree: '08:30',
            heureSortie: '17:30',
            statut: 'PRESENT'
          },
          {
            id: 2,
            employeeId: 2,
            employeeName: 'Marie Martin',
            datePresence: '2024-01-15',
            heureEntree: '09:15',
            statut: 'RETARD',
            motifAbsence: 'Retard transport'
          },
          {
            id: 3,
            employeeId: 3,
            employeeName: 'Paul Durand',
            datePresence: '2024-01-15',
            statut: 'ABSENT',
            motifAbsence: 'Maladie'
          }
        ];
        this.dataSource.data = this.presences;
        this.isLoading = false;
      }
    });
  }

  loadStats(): void {
    // Get today's date
    const today = new Date().toISOString().split('T')[0];

    this.http.get<any>(`${this.apiUrl}/stats?date=${today}`).subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
        // Mock stats
        this.stats = {
          totalPresences: 1247,
          presentsToday: 18,
          absentsToday: 7,
          retardsToday: 3
        };
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  getStatusClass(statut: string): string {
    switch (statut) {
      case 'PRESENT': return 'status-present';
      case 'ABSENT': return 'status-absent';
      case 'RETARD': return 'status-retard';
      case 'DEMI_JOURNEE': return 'status-demi';
      default: return '';
    }
  }

  getStatusLabel(statut: string): string {
    switch (statut) {
      case 'PRESENT': return 'Présent';
      case 'ABSENT': return 'Absent';
      case 'RETARD': return 'Retard';
      case 'DEMI_JOURNEE': return 'Demi-journée';
      default: return statut;
    }
  }

  refreshData(): void {
    this.loadPresences();
    this.loadStats();
  }
}
